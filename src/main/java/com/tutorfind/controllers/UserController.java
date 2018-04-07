package com.tutorfind.controllers;

import com.tutorfind.model.UserDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

@RestController
public abstract class UserController {

    @Autowired
    private DataSource dataSource;


    private static final Random RANDOM = new SecureRandom();

    public ArrayList<UserDataModel> getActiveUsersFromDB() {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            ArrayList<UserDataModel> output = new ArrayList();

            while (rs.next()) {
                output.add(new UserDataModel(rs.getInt("userId"), rs.getString("userName"), rs.getString("email"),
                        rs.getString("salt"), rs.getString("passhash"), rs.getString("userType")));
            }

            return output;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;

        }
    }



    public void insertUserIntoDB(String  userName, String email, String salt, String passhash, String userType){
        try (Connection connection = dataSource.getConnection()) {

            String query = "insert into users VALUES (DEFAULT,?,?,gen_salt('bf'),crypt(?,gen_salt('bf')),?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passhash);
            preparedStatement.setString(4, userType);
            preparedStatement.executeUpdate();
            connection.close();


        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public void updateUserFromDB(int userId,String username, String email, String salt, String passhash, String usertype){
        try (Connection connection = dataSource.getConnection()) {
            //Statement stmt = connection.createStatement();
            String query = "update users set username = ?, email = ?, salt = ?, passhash = ?, usertype = ? where userId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,email);
            preparedStatement.setString(3,salt);
            preparedStatement.setString(4,passhash);
            preparedStatement.setString(5,usertype);
            preparedStatement.setInt(6,userId);
            preparedStatement.executeUpdate();
            connection.close();


        } catch (SQLException e) {
            e.printStackTrace();


        }
    }


}
