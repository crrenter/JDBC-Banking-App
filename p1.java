//P1
//Section 5
//Student ID: 006152874
//Renteria, Carlos,

import java.util.*;
import java.sql.*;
import java.io.*;

public class p1 {
	
	private static String db, user, pwd, driver;
	private static int opt;
	private static Scanner in = new Scanner(System.in);
	private static Scanner input = new Scanner(System.in);
	
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
		boolean done = false;
		while (!done) {
			System.out.println("Welcome to the Self Service Banking System!" +
					"\n1. New Customer" +
					"\n2. Customer Login" +
					"\n3. Exit");
			/*Get user input*/
			opt  = in.nextInt();
			switch (opt) {
			case 1:
				//Prompt for Name Gender Age and Pin, return Id if successful
				int id = -1;
				System.out.print("Name: ");
				String name = input.nextLine();
				boolean finished = false;
				String gender = "";
				while (!finished) {
					System.out.print("Gender (M/F): ");
					gender = input.nextLine();
					if ("mMfF".contains(gender))
						finished = true;
				}
				System.out.print("Age: ");
				int age = in.nextInt();
				System.out.print("Pin: ");
				int pin = in.nextInt();
				//Do db call with an insert then return id
				id = newCust(name, gender, age, pin, stmt);
				System.out.println("YOUR ID IS: " + id);
				break;
			case 2:
				//Prompt for customer ID and pin to authenticate,
				//if customer enters 0 for ID and pin go to screen #4
				System.out.print("ID: ");
				int custId = in.nextInt();
				System.out.print("Pin: ");
				int custPin = in.nextInt();
				if (custId == 0 && custPin == 0) {
					adminLogin(stmt);
				}
				else if (authenticated(custId, custPin, stmt)) {
					customerScreen(custId, custPin, stmt);
				} else {
					System.out.println("WRONG ID OR PIN.");
				}
				break;
			case 3:
				//Exit
				done = true;
				break;
			default:
				System.out.println("Please choose a valid option (1, 2, or 3).");
				break;
			}
		}
	}

	private static void adminLogin(Statement stmt) throws SQLException {
		boolean done = false;
		int id;
		while (!done) {
			System.out.println("Administrator Main Menu " +
					"\n1. Account Summary for a Customer " +
					"\n2. Report A :: Customer Information with Total Balance in Decreasing Order " +
					"\n3. Report B :: Find the Average Total Balance Between Age Groups" +
					"\n4. Exit");
			opt = in.nextInt();
			switch (opt) {
			case 1:
				//Get account summary for a customer, provide a customer id
				System.out.print("ID: ");
				id = in.nextInt();
				accSummary(id, stmt);
				break;
			case 2:
				//You would display customer Id, Name, age, gender and total balance
				//System.out.print("ID: ");
				//id = in.nextInt();
				custInfo(stmt);//id, stmt);
				break;
			case 3: 
				//Find average total balance between age groups
				int minAge, maxAge;
				System.out.println("Min age: ");
				minAge = in.nextInt();
				System.out.println("Max age: ");
				maxAge = in.nextInt();
				avgTotalBal(minAge, maxAge, stmt);
				break;
			case 4:
				//Exit
				done = true;
				break;
			default:
				System.out.println("Please choose a valid option (1, 2, 3, or 4).");
			}
		}
	}

	private static void avgTotalBal(int minAge, int maxAge, Statement stmt) throws SQLException {
		String sqlAvgTotal = "select avg(age) as \"Average Age\", avg(balance) as \"Average Balance\" "+
				"from p1.customer as C, p1.account as A "+
				"where C.age >= " + minAge + " and C.age <= "+ maxAge +
				" and A.status = 'A'";
		ResultSet rs = stmt.executeQuery(sqlAvgTotal);
		int age, balance;
		System.out.println("AVERAGE AGE\tAVERAGE BALANCE");
		while (rs.next()) {
			age = rs.getInt("Average age");
			balance = rs.getInt("Average Balance");
			System.out.println(age + "\t\t" + balance);
		}
		rs.close();
	}

	private static void custInfo(Statement stmt) throws SQLException {
		String sqlInfo  = "select C.ID, Name, Age, Gender, sum(balance) as \"TOTAL BALANCE\" "+
				"from p1.customer as C, p1.account as A " +
				"where C.id = A.id and A.status = 'A'" +
				"group by C.ID, Name, Age, Gender " +
				"order by \"TOTAL BALANCE\" desc";
		String name, gender;
		int id, age, total;
		ResultSet rs = stmt.executeQuery(sqlInfo);
		System.out.println("ID\tNAME\t\tAGE\tGENDER\tTOTAL BALANCE");
		while (rs.next()) {
			id = rs.getInt("ID");
			name = rs.getString("Name");
			age = rs.getInt("Age");
			gender = rs.getString("Gender");
			total = rs.getInt("TOTAL BALANCE");
			if (name.length() >= 10)
				System.out.println(id +"\t" + name + "\t" + age + "\t" + gender + "\t" + total);
			else 
				System.out.println(id +"\t" + name + "\t\t" + age + "\t" + gender + "\t" + total);
		}
		rs.close();
	}

	private static void customerScreen(int custId, int custPin, Statement stmt) throws SQLException {
		boolean done = false;
		int balance, accNum, accNum2, id, amount;
		String accType;
		while (!done) {
			System.out.println("Customer Main Menu" +
					"\n1. Open Account" +
					"\n2. Close Account" +
					"\n3. Deposit" +
					"\n4. Withdraw" +
					"\n5. Transfer" +
					"\n6. Account Summary" +
					"\n7. Exit");
			opt = in.nextInt();
			switch (opt) {
			case 1:
				//Prompt for id, account type, initial balance
				//returns account number if successful.
				System.out.print("Enter an ID: ");
				id = in.nextInt();
				System.out.print("What type of account (C)hecking, or (S)avings: ");
				accType = input.nextLine();
				System.out.print("Enter your initial balance: ");
				balance = in.nextInt();
				accNum = newAccount(id, accType, balance, stmt);
				System.out.println("Your account number is: " + accNum);
				break;
			case 2:
				//Prompt for account number, then change the status to I
				//and empty the account balance
				System.out.print("Enter an account number: ");
				accNum = in.nextInt();
				closeAccount(custId, accNum, stmt);
				break;
			case 3:
				//Prompt for account number and amount to deposit
				System.out.print("Enter an account number: ");
				accNum = in.nextInt();
				System.out.print("Enter the amount to deposit: ");
				amount = in.nextInt();
				deposit(accNum, amount, stmt);
				break;
			case 4:
				//prompt for account number and amount to withdraw
				System.out.print("Enter your account number: ");
				accNum = in.nextInt();
				System.out.print("Enter the amount to withdraw: ");
				amount = in.nextInt();
				if (!validAcc(accNum, stmt)) {
					System.out.println("Invalid account number.");
				} else {
					withdraw(custId, accNum, amount, stmt);
				}
				break;
			case 5: 
				//prompt for the source and destination account number and amount
				System.out.print("Enter source account number: ");
				accNum = in.nextInt();
				System.out.print("Enter destination account number: ");
				accNum2 = in.nextInt();
				System.out.print("Enter transfer amount: ");
				amount = in.nextInt();
				if (validAcc(accNum, stmt) && validAcc(accNum2, stmt)) {
					transfer(custId, accNum, accNum2, amount, stmt);
				} else {
					System.out.println("Invalid account number(s).");
				}
				break;
			case 6:
				//The total balance and each account number and its balance
				//that belong to the same customer
				accSummary(custId, stmt);
				break;
			case 7:
				//Go back to the previous screen
				done = true;
				break;
			default:
				System.out.println("Please choose a valid option (1, 2, 3, 4, 5, 6, or 7).");
				break;
			}
		}
	}
	
	private static boolean isOwner(int id, int accNum, Statement stmt) throws SQLException {
		String sqlGetID = "select id from p1.account where number = "+accNum;
		ResultSet rs = stmt.executeQuery(sqlGetID);
		boolean owner = false;
		while (rs.next()) {
			owner = id == rs.getInt("ID") ? true : false;
		}
		rs.close();
		return owner;
	}

	private static void accSummary(int custId, Statement stmt) throws SQLException {
		String sqlSummary = "select sum(balance) as \"TOTAL BALANCE\" from p1.account where id = "+custId;
		int totalBal = -1, accNum, balance;
		ResultSet rs1 = stmt.executeQuery(sqlSummary);
		while (rs1.next()) {
			totalBal = rs1.getInt("TOTAL BALANCE");
		}
		
		sqlSummary = "select number, balance from p1.account where id ="+custId + " and status = 'A'";
		ResultSet rs2 = stmt.executeQuery(sqlSummary);
		System.out.println("TOTAL BALANCE\tACCOUNT\tBALANCE");
		String output = totalBal+"";
		while(rs2.next()) {
			accNum = rs2.getInt("Number");
			balance = rs2.getInt("Balance");
			output += "\t\t"+accNum +"\t"+balance;
			System.out.println(output);
			output = "";
		}
		rs1.close();
		rs2.close();
	}

	private static void transfer(int custId, int accNum, int accNum2, int amount,
			Statement stmt) throws SQLException {
		if (isOwner(custId, accNum, stmt)) {
			withdraw(custId, accNum, amount, stmt);
			deposit(accNum2, amount, stmt);
		} else {
			System.out.println("You cannot transfer from an account that you do not own.");
		}
	}

	private static void withdraw(int id, int accNum, int amt, Statement stmt) throws SQLException {
		if (isOwner(id, accNum, stmt)) {
			String sqlWithdraw = "update p1.account set balance = " +
					"(select balance from p1.account where number = "+accNum+")-"+amt+" where number = "+accNum;
			stmt.executeUpdate(sqlWithdraw);
		} else {
			System.out.println("You are not the owner of " + accNum);
		}
	}

	private static boolean validAcc(int accNum, Statement stmt) throws SQLException {
		String sqlValidateAcc = "select id from p1.account where number = "+accNum;
		ResultSet rs = stmt.executeQuery(sqlValidateAcc);
		int id = -1;
		while (rs.next()) {
			id = rs.getInt("ID");
		}
		rs.close();
		return id >= 100 ? true : false;
	}

	private static void deposit(int accNum, int amt, Statement stmt) throws SQLException {
		String sqlDeposit = "update p1.account set balance =" +
				"(select balance from p1.account where number ="+accNum+")+"+amt+" where number = "+accNum;
		stmt.executeUpdate(sqlDeposit);
	}

	private static void closeAccount(int id, int accNum, Statement stmt) throws SQLException {
		if (isOwner(id, accNum, stmt)) {
			String sqlClose = "update p1.account set status = 'I', balance = 0 where number =" + accNum;
			stmt.executeUpdate(sqlClose);
			System.out.println("Account "+accNum+" has been closed.");
		} else {
			System.out.println("You are not the owner of " + accNum);
		}
	}

	private static int newAccount(int id, String accType, int inBalance,
			Statement stmt) throws SQLException {
		int accNum = -1;
		String sqlMakeAcc = "insert into p1.account (id, balance, type, status) " +
				"values ("+id+","+inBalance+",'"+accType+"','A')";
		stmt.executeUpdate(sqlMakeAcc);
		String getAccNum = "select Number from p1.account where id ='"+id+"'";
		ResultSet rs = stmt.executeQuery(getAccNum);
		while(rs.next()) {
			accNum = rs.getInt("Number");
		}
		return accNum;
	}

	private static boolean authenticated(int id, int pin, Statement stmt) throws SQLException {
		String sqlVerify = "select pin from p1.customer where id ='" + id + "'";
		ResultSet rs = stmt.executeQuery(sqlVerify);
		int thePin = -1;
		while (rs.next()) {
			thePin = rs.getInt("Pin");
		}
		rs.close();
		return thePin == pin ? true : false;
	}

	private static int newCust(String name, String gender, int age,
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
