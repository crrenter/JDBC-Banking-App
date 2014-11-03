import java.util.*;
import java.sql.*;
import java.io.*;

public class p1 {
	
	private static String db;
	private static String user;
	private static String pwd;
	private static String driver;
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
				System.out.println("Please enter you name, " +
						"\nGender (M/F), " +
						"\nAge, " +
						"\nPin");
				String name = input.nextLine();
				String gender = input.nextLine();
				int age = in.nextInt();
				int pin = in.nextInt();
				//Do db call with an insert then return id
				int id = newCust(name, gender, age, pin, stmt);
				System.out.println("YOUR ID IS: " + id);
				break;
			case 2:
				//Prompt for customer ID and pin to authenticate,
				//if customer enters 0 for ID and pin go to screen #4
				System.out.println("Enter your ID\nand Pin");
				int custId = in.nextInt();
				int custPin = in.nextInt();
				if (custId == 0 && custPin == 0) {
					adminLogin();
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

	private static void adminLogin() {
		boolean done = false;
		while (!done) {
			System.out.println("Administrator Main Menu " +
					"\n1. Account Summary for a Customer " +
					"\n2. Report A :: Customer Information with Total Balance in Decreaseing Order " +
					"\n3. Report B :: Find the Average Total Balance Between Age Groups" +
					"\n4. Exit");
			opt = in.nextInt();
			switch (opt) {
			case 1:
				break;
			case 2:
				break;
			case 3: 
				break;
			case 4:
				done = true;
				break;
			default:
				System.out.println("Please choose a valid option (1, 2, 3, or 4).");
			}
		}
		
		
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
				System.out.println("Enter your id");
				id = in.nextInt();
				System.out.println("What type of account (C)hecking, or (S)avings.");
				accType = input.nextLine();
				System.out.println("Enter your initial balance");
				balance = in.nextInt();
				accNum = newAccount(id, accType, balance, stmt);
				System.out.println("Your account number is: " + accNum);
				break;
			case 2:
				//Prompt for account number, then change the status to I
				//and empty the account balance
				System.out.print("Enter your account number: ");
				accNum = in.nextInt();
				closeAccount(accNum, stmt);
				System.out.println("Account "+accNum+" has been closed.");
				break;
			case 3:
				//Prompt for account number and amount to deposit
				System.out.print("Enter your account number: ");
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
					withdraw(accNum, amount, stmt);
				}
				break;
			case 5: 
				//prompt for the source and destination account number and amount
				System.out.print("Enter source account number: ");
				accNum = in.nextInt();
				System.out.println("Enter destination account number: ");
				accNum2 = in.nextInt();
				System.out.println("Enter transfer amount: ");
				amount = in.nextInt();
				if (validAcc(accNum, stmt) && validAcc(accNum2, stmt)) {
					transfer(accNum, accNum2, amount, stmt);
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

	private static void accSummary(int custId, Statement stmt) throws SQLException {
		String sqlSummary = "select sum(balance) as TotalBalance from p1.account where id = "+custId;
		int totalBal = -1;
		ResultSet rs = stmt.executeQuery(sqlSummary);
		while (rs.next()) {
			totalBal = rs.getInt("TotalBalance");
		}
		System.out.println("Total balance: " + totalBal);
		
		//sqlSummary = "select "
		rs.close();
	}

	private static void transfer(int accNum, int accNum2, int amount,
			Statement stmt) throws SQLException {
		withdraw(accNum, amount, stmt);
		deposit(accNum2, amount, stmt);
	}

	private static void withdraw(int accNum, int amt, Statement stmt) throws SQLException {
		String sqlWithdraw = "update p1.account set balance = " +
				"(select balance from p1.account where number = "+accNum+")-"+amt+" where number = "+accNum;
		stmt.executeUpdate(sqlWithdraw);
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

	private static void closeAccount(int accNum, Statement stmt) throws SQLException {
		String sqlClose = "update p1.account set status = 'I', balance = 0 where number =" + accNum;
		stmt.executeUpdate(sqlClose);
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
