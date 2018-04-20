package com.tutorfind.controllers;


import com.tutorfind.exceptions.ResourceNotFoundException;
import com.tutorfind.model.TutorsDataModel;
import com.tutorfind.model.UserDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

@CrossOrigin
@RestController
@RequestMapping("tutors")
public class TutorController extends UserController{

    @Autowired
    private DataSource dataSource;

    private ArrayList<TutorsDataModel> getActiveTutorsFromDB() {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM tutors WHERE active IS TRUE ORDER BY creationdate DESC");

            ArrayList<TutorsDataModel> output = new ArrayList();

            while (rs.next()) {
                String ratingsString = rs.getString("rating");
                if (ratingsString.length() != 2) {
                    String[] r = ratingsString.split(",");
                    Integer[] ratings = new Integer[r.length];
                    for (int i = 0; i < r.length; i++) {
                        if (i == 0) {
                            ratings[i] = Integer.parseInt(r[i].substring(1));
                        } else if (i == r.length - 1) {
                            ratings[i] = Integer.parseInt(r[i].substring(0, 1));
                        } else {
                            ratings[i] = Integer.parseInt(r[i]);
                        }
                    }
                    output.add(new TutorsDataModel(rs.getInt("userId"), rs.getString("legalfirstname"), rs.getString("legallastname"), rs.getString("bio"), rs.getString("degrees"), rs.getString("links"), rs.getString("img"), rs.getBoolean("active"), rs.getTimestamp("creationdate"), ratings));

                }else {
                   Integer[] ratings = new Integer[0];
                    output.add(new TutorsDataModel(rs.getInt("userId"), rs.getString("legalfirstname"), rs.getString("legallastname"), rs.getString("bio"), rs.getString("degrees"), rs.getString("links"), rs.getString("img"), rs.getBoolean("active"), rs.getTimestamp("creationdate"), ratings));

                }


            }
            return output;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;

        }
    }


    public void updateTutorsFromDB(int userId, String legalFirstName,String legalLastName, String bio, String degrees, String links,String img, boolean active, Timestamp creationdate, Integer[] ratings){
        try (Connection connection = dataSource.getConnection()) {
            final java.sql.Array sqlArray = connection.createArrayOf("integer", ratings);
           
            //Statement stmt = connection.createStatement();
            String query = "update tutors set legalFirstName = ?, legalLastName = ?, bio = ?, degrees = ?, links = ?, img = ?, active = ?, creationdate = ?, rating = ? where userId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(2, legalLastName);
            preparedStatement.setString(1, legalFirstName);
            preparedStatement.setString(3, bio);
            preparedStatement.setString(4, degrees);
            preparedStatement.setString(5, links);
            preparedStatement.setString(6, img);
            preparedStatement.setBoolean(7,active);
            preparedStatement.setTimestamp(8, creationdate);
            preparedStatement.setArray(9,sqlArray);
            preparedStatement.setInt(10,userId);
            preparedStatement.executeUpdate();
            connection.close();


        } catch (SQLException e) {
            e.printStackTrace();


        }
    }

    public void insertTutorIntoDB(int userId, String legalFirstName,String legalLastName, String bio, String degrees, String links,String img, boolean active, Timestamp timestamp, Integer[] ratings){
        try (Connection connection = dataSource.getConnection()) {
            //Statement stmt = connection.createStatement();

            final java.sql.Array sqlArray = connection.createArrayOf("integer", ratings);
            String query = "insert into tutors VALUES(?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(3, legalLastName);
            preparedStatement.setString(2, legalFirstName);
            preparedStatement.setString(4, bio);
            preparedStatement.setString(5, degrees);
            preparedStatement.setString(6, links);
            preparedStatement.setString(7, img);
            preparedStatement.setBoolean(8,active);
            preparedStatement.setTimestamp(9, timestamp);
            preparedStatement.setArray(10, sqlArray);
            preparedStatement.setInt(1,userId);

            preparedStatement.executeUpdate();
            connection.close();


        } catch (SQLException e) {
            e.printStackTrace();


        }
    }


    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody ArrayList<TutorsDataModel> printTutors() {

        ArrayList<TutorsDataModel> tutors = getActiveTutorsFromDB();

        ArrayList<UserDataModel> users = getActiveUsersFromDB();

        for(TutorsDataModel tutor : tutors){
            for(UserDataModel user : users){
                if(tutor.getUserId() == user.getUserId()){
                    tutor.setUserType(user.getUserType());
                    tutor.setPasshash(user.getPasshash());
                    tutor.setEmail(user.getEmail());
                    tutor.setSalt(user.getSalt());
                    tutor.setUserName(user.getUserName());
                    tutor.setSubjects(user.getSubjects());
                }
            }

        }
        return tutors;
    }

    @RequestMapping(value = "{id}",method = RequestMethod.GET)
    public @ResponseBody TutorsDataModel printTutor(@PathVariable("id") int id) {

        TutorsDataModel tutor = new TutorsDataModel();

        ArrayList<TutorsDataModel> tutors = getActiveTutorsFromDB();
        ArrayList<UserDataModel> users = getActiveUsersFromDB();

        for(TutorsDataModel t : tutors){
            for(UserDataModel user : users){
                if(t.getUserId() == user.getUserId()){
                    t.setUserType(user.getUserType());
                    t.setPasshash(user.getPasshash());
                    t.setSalt(user.getSalt());
                    t.setUserName(user.getUserName());
                    t.setEmail(user.getEmail());
                    t.setSubjects(user.getSubjects());
                }
            }

        }

        for(TutorsDataModel t : tutors){
            if(t.getUserId() == id){
                tutor = t;
            }
        }
        if(tutor.getUserName() == null) {
            throw new ResourceNotFoundException();
        }else {
            return tutor;
        }
       

    }

    @RequestMapping(value = "login", method = {RequestMethod.POST})
    public TutorsDataModel loginStudent(@RequestBody TutorsDataModel t){
        ArrayList<TutorsDataModel> tutors = getActiveTutorsFromDB();
        ArrayList<UserDataModel> users = getActiveUsersFromDB();
        try (Connection connection = dataSource.getConnection()) {

            String sql = "SELECT userId, passhash = crypt(?, passhash) as pass, passhash from users where passhash = crypt(?, passhash) AND username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(2,t.getPasshash());
            preparedStatement.setString(1,t.getPasshash());
            preparedStatement.setString(3,t.getUserName());
            ResultSet rs = preparedStatement.executeQuery();


            ArrayList<TutorsDataModel> output = new ArrayList<TutorsDataModel>();

            while (rs.next()) {
                for(TutorsDataModel tutor : tutors){
                    for(UserDataModel u : users){
                        if(tutor.getUserId() == u.getUserId()){
                            if(rs.getInt("userId") == tutor.getUserId()) {
                                tutor.setEmail(u.getEmail());
                                tutor.setUserName(u.getUserName());
                                tutor.setSalt(u.getSalt());
                                tutor.setUserType(u.getUserType());
                                tutor.setPasshash(u.getPasshash());
                                tutor.setSubjects(u.getSubjects());
                                output.add(tutor);

                            }
                        }

                    }

                }

            }



            if(output.isEmpty()){
                throw new ResourceNotFoundException();
            }else {
                return output.get(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;

        }

    }


    @RequestMapping(method = {RequestMethod.PUT})
    public ResponseEntity<TutorsDataModel> insertTutor(@RequestBody TutorsDataModel t) {
        ArrayList<UserDataModel> users = getActiveUsersFromDB();
        for(UserDataModel user : users){
            if(user.getUserName().equals(t.getUserName()) || user.getEmail().equals(t.getEmail())){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        insertUserIntoDB(t.getUserName(),t.getEmail(),t.getSalt(),t.getPasshash(),t.getUserType(),t.getSubjects());
        users = getActiveUsersFromDB();
        for(UserDataModel user : users){
            if(user.getUserName().equals(t.getUserName()))
                insertTutorIntoDB(user.getUserId(),t.getLegalFirstName(),t.getLegalLastName(),t.getBio(),t.getDegrees(),t.getLinks(),t.getImg(),t.getActive(),t.getTimestamp(),t.getRatings());
        }

        return new ResponseEntity<>(HttpStatus.OK);


    }

    @RequestMapping(value = "{tutorId}", method = {RequestMethod.POST})
    public ResponseEntity<TutorsDataModel> updateTutor(@PathVariable("tutorId") int id, @RequestBody TutorsDataModel t) {

        updateTutorsFromDB(id,t.getLegalFirstName(),t.getLegalLastName(),t.getBio(),t.getDegrees(),t.getLinks(),t.getImg(),t.getActive(),t.getTimestamp(),t.getRatings());
        updateUserFromDB(id,t.getUserName(),t.getEmail(),t.getUserType(),t.getSubjects());
        return new ResponseEntity<>(HttpStatus.OK);


    }

}
