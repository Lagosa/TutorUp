package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.Map;

public interface UserDao {

    Map<String, Object> findById(int id)throws SQLException;

    /**
     * Search for a given user based on token.
     *
     * @param token the token of the user
     * @return the user if found, null if not found
     * @throws SQLException in case of DB error
     */
    Map<String, Object> findByToken(String token) throws SQLException;

    /**
     * Performs user authentication.
     *
     * @param email    the user e-mail address
     * @param password the user password
     * @return the authenticated user, null if user not found
     * @throws SQLException in case of DB error
     */
    Map<String, Object> findByEmailAndPassword(String email, String password) throws SQLException;

    /**
     * Performs user registration.
     *
     * @param first_name the user first name
     * @param last_name the user last name
     * @param birth_year the user birth year
     * @param birth_month the user birth month
     * @param birth_day the user birth day
     * @param username the user username
     * @param password the user password
     * @param email the user email address
     * @param user_recommend the user name who recommended the app
     * @param language the language spoken by the user
     * @return the registered user
     * @throws SQLException in case of DB error
     */
    Map<String, Object> registration(String first_name, String last_name, Integer birth_year, Integer birth_month,
                                     Integer birth_day, String username, String password, String email,
                                     String language, String token, String phone_number, String skill, String city) throws SQLException;

    /**
     *
     * @param new_password the user new password
     * @param old_password the user old password
     * @param id the user unique id
     * @return false in case of fail, true in case of pass
     * @throws SQLException in case of DB error
     */
    Boolean change_password(String new_password, String old_password, int id) throws SQLException;

    /**
     *
     * @param first_name the user first name
     * @param last_name the user last name
     * @param birth_year the user birth_year
     * @param birth_month the user birth month
     * @param birth_day the user birth day
     * @param username the user username
     * @param email the user email address
     * @param token the user token OPTIONAL
     * @param id the user unique id
     * @return user updated data, null if failed
     * @throws SQLException in case of DB error
     */

    Map<String, Object> update(String first_name, String last_name, Integer birth_year, Integer birth_month,
                               Integer birth_day, String username, String email, String token, int id, String password,
                               String status, String language, String phone_number, String skill, String city,
                               String picture_url, String bio, String edu_backg) throws SQLException;


    Map<String, Object> findByEmail(String email) throws SQLException;

    Map<String,Object> findByUsername(String username) throws SQLException;
 }

