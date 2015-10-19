/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              boolean OpenChat = true;
              for (int i = 0; i <25; i++){
                  System.out.println("");
              }              
              while(usermenu) {

                System.out.println("USER MENU");
                System.out.println("---------");
                System.out.println("1. Add to contact list");
                System.out.println("2. Browse contact list");
                System.out.println("3. Read notification list");
                System.out.println("4. Browse block list");
                System.out.println("5. Delete this account");
                System.out.println("6. Add to block list");
                System.out.println("7. Browse chats");
                System.out.println("8. Open Chat");
                System.out.println("9. Start new chat");
                System.out.println(".........................");
                System.out.println("10. Log out");
                switch (readChoice()){
                   case 1: AddToContact(esql,authorisedUser); cls(); break;
                   case 2: ListContacts(esql, authorisedUser); cls(); break;
                   case 3: ReadNotifications(esql, authorisedUser); cls(); break;
                   case 4: ViewBlock(esql, authorisedUser); cls(); break;
                   case 5: DeleteAccount(esql, authorisedUser); cls(); break;
                   case 6: AddBlock(esql, authorisedUser); cls(); break;
                   case 7: ShowChat(esql,authorisedUser); cls(); break;
                   case 8: OpenChat = false; break;
                   case 9: StartNewChat(esql, authorisedUser); break;
                   case 10: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
                if (OpenChat == false)
                {
                    try {
                        System.out.println("Enter chat id");
                        Scanner in = new Scanner(System.in);
                        int cid1 = in.nextInt();
    //CHECKER
                        String query = String.format("SELECT * FROM CHAT_LIST WHERE member='%s' AND chat_id='%s'", authorisedUser, cid1);
                        int check = esql.executeQuery(query);
                        if (check > 0) {
                                            boolean ChatOn = true;
                          for (int i = 0; i <25; i++){
                              System.out.println("");
                          }    
                            while (ChatOn)
                            {
                                
                                System.out.println("CHAT MENU");
                                System.out.println("---------");
                                System.out.println("1. Browse members of chat");
                                System.out.println("2. Add member to chat");
                                System.out.println("3. Delete member from chat");
                                System.out.println("4. Delete entire chat");
                                System.out.println("5. View Messages");
                                System.out.println("6. Create New Message");
                                System.out.println("7. Delete Message");
                                System.out.println("8. Edit Message");
                                System.out.println(".........................");
                                System.out.println("10. EXIT CHAT");
                                switch(readChoice())
                                {
                                    case 1: BrowseChatMembers(esql, authorisedUser,cid1);cls(); break;
                                    case 2: AddMemberToChat(esql, authorisedUser, cid1); cls();break;
                                    case 3: DeleteMemberFromChat(esql, authorisedUser, cid1); cls();break;
                                    case 4: DeleteEntireChat(esql, authorisedUser, cid1); cls(); break;
                                    case 5: ViewMessages(esql, authorisedUser, cid1); break;
                                    case 6: CreateMessage(esql, authorisedUser, cid1); break;
                                    case 7: DeleteMessage(esql, authorisedUser, cid1); cls(); break;
                                    case 8: EditMessage(esql, authorisedUser, cid1); cls(); break;
                                    case 10: ChatOn = false; OpenChat=true; break;
                                    default: System.out.println("Invalid choice"); cls();break;
                                }
                            }
                        }
                        else 
                        {
                            System.out.println("Invalid chat id.");
                            OpenChat = true;
                        }
                    }
            catch (Exception e) {
                System.err.println(e.getMessage());
    }

                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  public static void cls()
  {
      try{
          System.out.println("Press enter to continue.");
          String wait = in.readLine();
          for (int i = 0; i <25; i++){
              System.out.println("");
          }
      }catch (Exception e){
          System.err.println(e.getMessage());
      }
  }
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         System.out.print("\tEnter user status: ");
         String sss = in.readLine();
        if(sss.isEmpty()) sss = "";
        //Creating empty contact\block lists for a user
        esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
        int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
        esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
        int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
        String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list, status) VALUES ('%s','%s','%s',%s,%s,'%s')", phone, login, password, block_id, contact_id, sss);
        esql.executeUpdate(query);
        System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   ///////////////////////////// USER METHODS ////////////////////////////////////////////////////
//CASE1
   public static void AddToContact(Messenger esql,String auth){
    try {
         System.out.print("\tEnter Contact Name: ");
         String cname = in.readLine();

         //check if user exists
         String query = String.format("SELECT * FROM Usr WHERE login = '%s'", cname);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
        String query1 = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member) SELECT contact_list,'%s' FROM Usr WHERE login='%s'", cname, auth);
         esql.executeUpdate(query1);
         System.out.println ("User added successfully!");
        }
    else System.out.println ("User doesn't exist or failure.");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

//CASE2
   public static void ListContacts(Messenger esql, String auth){
        try {
       String query = String.format("SELECT UL.list_member , U.status FROM USER_LIST_CONTAINS UL, Usr U WHERE UL.list_member=U.login AND UL.list_id IN (SELECT U3.contact_list FROM Usr U3 WHERE U3.login='%s')", auth);
        esql.executeQueryAndPrintResult(query);

    } catch(Exception e){
        System.err.println (e.getMessage ());
    }
         
   }//end
   
    //CASE3
   public static void ReadNotifications(Messenger esql, String auth){
    try {
            String query = String.format("SELECT N.msg_id FROM NOTIFICATION N, USR U WHERE U.login=N.usr_login AND N.usr_login='%s'", auth);
            esql.executeQueryAndPrintResult(query);            
            
            String query1 = String.format("DELETE FROM NOTIFICATION WHERE usr_login='%s'", auth);
            esql.executeUpdate(query1);
    }catch(Exception e){
        System.out.println(e.getMessage());
    }
   }//end  
   
   //CASE4
    public static void ViewBlock(Messenger esql, String auth){
        try {
        String query = String.format("SELECT list_member FROM USER_LIST_CONTAINS WHERE list_id IN (SELECT block_list FROM Usr WHERE login='%s')", auth);
        esql.executeQueryAndPrintResult(query);            
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    //CASE 5
    public static void DeleteAccount(Messenger esql, String auth){
        try {
                String query = String.format("DELETE FROM Usr WHERE login='%s'", auth);
                esql.executeUpdate(query);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

//CASE 6
   public static void AddBlock(Messenger esql, String auth){
    try {
         System.out.print("\tEnter Contact Name to Block: ");
         String cname = in.readLine();

         //check if user exists
         String query = String.format("SELECT * FROM Usr WHERE login = '%s'", cname);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
        String query1 = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member) SELECT block_list,'%s' FROM Usr WHERE login='%s'", cname, auth);
         esql.executeUpdate(query1);
         System.out.println ("User added successfully!");
        }
    else System.out.println ("User doesn't exist or failure.");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }
///////////////////////////// CHAT METHODS /////////////////////////////////////////  
  //CASE 1
   public static void ShowChat(Messenger esql, String auth){
    try {
         //String query = String.format("SELECT chat_id FROM CHAT_LIST WHERE member='%s'",auth);
         
         String query=String.format("SELECT C.chat_id, MAX(m.msg_timestamp) FROM CHAT_LIST C, MESSAGE M WHERE C.member='%s' AND C.chat_id= M.chat_id GROUP BY C.chat_id",auth);
        
         //String query = String.format("SELECT DISTINCT ON (chat_id) C.chat_id, m.msg_timestamp FROM CHAT_LIST C, MESSAGE M WHERE C.member='%s' ORDER BY m.msg_timestamp DESC",auth);

         esql.executeQueryAndPrintResult(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }
  //case 2
  public static void StartNewChat(Messenger esql, String auth) {
      try {
                        //insert newchat into CHAT
                        String query1 = String.format("INSERT INTO CHAT (chat_type, init_sender) VALUES ('private', '%s')", auth);
                        esql.executeUpdate(query1);
                        int cid= esql.getCurrSeqVal("chat_chat_id_seq");
                        //insert newchat into CHAT_LIST
                        String query2 = String.format("INSERT INTO CHAT_LIST (chat_id, member) VALUES ('%s', '%s')", cid, auth);
                        esql.executeUpdate(query2);
                        System.out.println(cid);
                        boolean true1 = true;
                        while(true1)
                        {
                            System.out.println("Enter additional member logins if desired. Type quit when done (one per line): ");
                            String adder = in.readLine();
                            if (adder.equals("quit")) true1=false;
                            else 
                            {
                                String checker = String.format("SELECT * FROM Usr WHERE login = '%s'", adder);
                                int userNum = esql.executeQuery(checker);
                                if (userNum > 0){
                                        String query3 = String.format("INSERT INTO CHAT_LIST (chat_id, member) VALUES ('%s', '%s')", cid, adder);
                                        esql.executeUpdate(query3);
                                        
                                    }
                                else System.out.println("Invalid entry, try again!");
                            }

                        }
                        CreateMessage(esql, auth, cid);
      }catch(Exception e) {
          System.err.println (e.getMessage());
      }
  }
     //case3
    public static void BrowseChatMembers(Messenger esql, String auth,int cid){
        try{
            
            String query = String.format("SELECT member FROM CHAT_LIST WHERE chat_id='%s'", cid);
            esql.executeQueryAndPrintResult(query);
        }catch(Exception e) {
            System.err.println (e.getMessage());
        }
            
   }//end      
    //case 4
   public static void AddMemberToChat(Messenger esql, String auth,int cid){
       try {
                System.out.println("Enter Member to add into chat: ");
                String mem = in.readLine();
                
                String querycheck = String.format("SELECT * FROM CHAT_LIST WHERE member='%s'", mem);
                int checker = esql.executeQuery(querycheck);
                if (checker > 0)
                {
                    String querya = String.format("INSERT INTO CHAT_LIST (chat_id, member) VALUES ('%s','%s')", cid, mem);
                    esql.executeUpdate(querya); 
                }
                else System.out.println("Invalid member name");
        }catch (Exception e) {
            System.err.println(e.getMessage());
            }
    }         
    //case 5  
  public static void DeleteMemberFromChat(Messenger esql, String auth,int cid){
       try {

                 System.out.println("Enter member to be deleted: ");
                String mem = in.readLine();
                
                String querycheck = String.format("SELECT * FROM CHAT_LIST WHERE member='%s'", mem);
                int checker = esql.executeQuery(querycheck);
                if (checker > 0)
                {
                    String queryd = String.format("DELETE FROM CHAT_LIST WHERE member='%s'", mem);
                    esql.executeUpdate(queryd); 
                }
                else System.out.println("Invalid member name");

        }catch (Exception e) {
            System.err.println(e.getMessage());
            }
    }     
//case 6
  public static void DeleteEntireChat(Messenger esql, String auth,int cid){
       try {
                System.out.println("Are you sure you want to delete this entire chat? Type yes or no");
                String check = in.readLine();
                if (check.equals("yes"))
                {
                    String querycl = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s'", cid);
                    String querym = String.format("DELETE FROM MESSAGE WHERE chat_id='%s'", cid);
                    String querychat = String.format("DELETE FROM CHAT WHERE chat_id='%s'", cid);
                    
                    esql.executeUpdate(querycl);
                    esql.executeUpdate(querym);
                    esql.executeUpdate(querychat);
                    System.out.println("The Chat has been deleted.");
                }
                else System.out.println("Chat was not deleted");
        }catch (Exception e) {
            System.err.println(e.getMessage());
            }
    }     
 //case 7
 public static void ViewMessages(Messenger esql, String auth,int cid) {
    try {

                    boolean trueloop = true;
                    int var1=0;
                    String query1 = String.format("SELECT DISTINCT M.sender_login, M.msg_timestamp, M.msg_text, A.media_type, A.URL FROM MESSAGE M, MEDIA_ATTACHMENT A WHERE M.chat_id='%s' AND A.msg_id=M.msg_id UNION SELECT DISTINCT M.sender_login, M.msg_timestamp, M.msg_text,NULL AS media_type, NULL AS URL FROM MESSAGE M, MEDIA_ATTACHMENT A WHERE M.chat_id='%s' ORDER BY msg_timestamp DESC limit 10", cid, cid);
                    esql.executeQueryAndPrintResult(query1);
                    
                    //self destruct messages
                    String query4 = "DELETE FROM MESSAGE WHERE destr_timestamp < msg_timestamp";
                    esql.executeUpdate(query4);
                    
                    while (trueloop)
                    {
                        System.out.println("Type more for 10 more messages. Or type quit to exit chat list.");
                        String input = in.readLine();
                        if (input.equals("quit"))
                        {
                            trueloop=false;
                            break;
                        }
                        else if (input.equals("more"))
                        {
                            var1+=10;
                            String query2 = String.format("SELECT DISTINCT M.sender_login, M.msg_timestamp, M.msg_text, A.media_type, A.URL FROM MESSAGE M, MEDIA_ATTACHMENT A WHERE M.chat_id='%s' AND A.msg_id=M.msg_id UNION SELECT DISTINCT M.sender_login, M.msg_timestamp, M.msg_text,NULL AS media_type, NULL AS URL FROM MESSAGE M, MEDIA_ATTACHMENT A WHERE M.chat_id='%s' ORDER BY msg_timestamp DESC limit 10 OFFSET '%s'", cid, cid, var1);

                            esql.executeQueryAndPrintResult(query2);
                            
                        }
                        else System.out.println("Invalid entry. Try again.");
                        
                    }
                    for (int i = 0; i <25; i++){
                    System.out.println("");
                    }    
    }catch (Exception e) {
        System.err.println(e.getMessage());
    }
}
//case 8
   public static void CreateMessage(Messenger esql, String auth,int cid) {
       try {
                System.out.println("Enter Message: ");
                String input = in.readLine();
                
                String query1 = String.format("INSERT INTO MESSAGE (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', 'now()', '%s', '%s')", input, auth, cid);
                esql.executeUpdate(query1);
                int msg1= esql.getCurrSeqVal("message_msg_id_seq");
              for (int i = 0; i <25; i++){
                  System.out.println("");
              }  
      }catch(Exception e) {
          System.err.println (e.getMessage());
      }          
}
//  CASE9
    public static void DeleteMessage(Messenger esql, String auth,int cid) {
        try {
            System.out.println("Enter message id: ");
            Scanner in1 = new Scanner(System.in);
            int input = in1.nextInt();          
            
            String query1 = String.format("SELECT * FROM MESSAGE WHERE msg_id='%s'", input);
            int valid = esql.executeQuery(query1);
            if (valid > 0) 
            {
                String query = String.format("DELETE FROM MESSAGE WHERE msg_id='%s'", input);
                esql.executeUpdate(query);
                System.out.println("Message has been deleted successfully");
            }
            else System.out.println("Invalid ID.");
        }catch (Exception e) {
            System.err.println (e.getMessage());
        }
    }
    public static void EditMessage(Messenger esql, String auth,int cid) {
       try {
                System.out.println("Enter message id: ");
                Scanner in = new Scanner(System.in);
                int inputid = in.nextInt();                 
                in.nextLine();
                
            String query1 = String.format("SELECT * FROM MESSAGE WHERE msg_id='%s'", inputid);
            int valid = esql.executeQuery(query1);

            
            if (valid > 0) 
            {                
                System.out.println("Enter edited message: ");
                String input = in.nextLine();
            
                String query = String.format("UPDATE MESSAGE SET msg_text='%s' WHERE msg_id='%s'", input, inputid);
                esql.executeUpdate(query);
             } 
             else System.out.println("Invalid ID.");
     }catch(Exception e) {
          System.err.println (e.getMessage());
      }         
        
             }
}//end Messenger
