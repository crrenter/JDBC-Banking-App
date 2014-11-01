package cs157a.p1;

import java.util.*;
import java.sql.*;
import java.io.*;

public class BankingJDBC {
	
	private static String db;
	private static String user;
	private static String pwd;
	private static String driver;
	private static int opt;
	private static Scanner in = new Scanner(System.in);
	
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Need props file.");
		}
		else {
			init(args[0]);
			
			try {
				Class.forName(driver);
				/*Connection var*/
				Connection conn = null;
				/*SQL statement var*/
				Statement stmt = null;
				conn = DriverManager.getConnection(db, user, pwd);
				stmt = conn.createStatement();
				
				mainScreen(stmt);
				
				stmt.close();
				conn.close();
				
			} catch (SQLException sqlE) {
				sqlE.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			
				
			}
		}
	}

	private static void init(String filename) {
	    try {
	      Properties props = new Properties();
	      FileInputStream input = new FileInputStream(filename);
	      props.load(input);
	      driver = props.getProperty("jdbc.driver");
	      db = props.getProperty("jdbc.url");
	      user = props.getProperty("jdbc.username"); 
	      pwd = props.getProperty("jdbc.password");
	    } catch (Exception e) {
	      System.out.println("Exception in init()");
	      e.printStackTrace();
	    }
	  }
	
	private static void mainScreen(Statement stmt) throws SQLException {
		System.out.println("Welcome to the Self Service Banking System!" +
				"\n1. New Customer" +
				"\n2. Customer Login" +
				"\n3. Exit");
		/*Get user input*/
		opt  = in.nextInt();
		switch (opt) {
		case 1:
			Scanner input = new Scanner(System.in);
			//Prompt for Name Gender Age and Pin, return Id if successful
			System.out.println("Please enter you name, " +
					"\nGender (M/F), " +
					"\nAge, " +
					"\nPin");
			String name = input.nextLine();
			String gender = input.nextLine();
			int age = in.nextInt();
			int pin = in.nextInt();
			//Do db call with an insert then return id
			int id = screenOneOptOne(name, gender, age, pin, stmt);
			System.out.println("Your ID is: " + id);
			break;
		case 2:
			//Go to option 2
			break;
		case 3:
			//Go to option 3
			break;
		default:
			System.out.println("Please choose a valid option (1, 2, or 3).");
			break;
		}
	}

	private static int screenOneOptOne(String name, String gender, int age,
			int pin, Statement stmt) throws SQLException {
		String sql = "insert into P1.Customer (Name, Gender, Age, Pin) values ('"+name+"','"+gender+"',"+age+","+pin+")";
		stmt.executeUpdate(sql);
		sql = "select id from P1.Customer where name='"+name+"' and gender='"+gender+"' and age='"+age+"'";
		ResultSet rs = stmt.executeQuery(sql);
		int id = 0;
		while (rs.next()) {
			id = rs.getInt("ID");
		}
		rs.close();
		return id;
	}
}
