import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Server
{
    ArrayList clientOutputStream;
    ArrayList<String> users;
    JFrame jFrame;
    ServerSocket serverSock = null;
    Socket clientSock=null;
    Thread starter=null,listener=null;
    PrintWriter writer=null;

    public Server()
    {
        jFrame = new JFrame("Multithreaded Chat - Server Side");
        jFrame.setSize(550,500);
        jFrame.add(jPanel);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        b_end.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                tellEveryone("Server:is stopping and all users will be disconnected\n:Chat");
                ta_chat.append("\nServer stopping...");
                try
                {
                    serverSock.close();
                    clientSock.close();         //to stop user inputs on server side
                    starter.interrupt();        //to avoid exception when serversocket is closed
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                ta_chat.append("\n*Server has been closed.*");
            }
        });

        b_start.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ta_chat.setText(" ");
                starter = new Thread(new ServerStart());            //Initializes Thread to ServerStart
                starter.start();                                    //Starts thread of Server which runs parallel to other threads
                ta_chat.append("Server started...");

            }
        });

        b_users.addActionListener(new ActionListener()
        {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                  ta_chat.append("\n Online users : \n");
                  for (String current_user: users)
                  {
                      ta_chat.append(current_user);
                      ta_chat.append("\n");
                  }
              }
          });

        b_clear.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ta_chat.setText(" ");
            }
        });

    }

    public static void main(String args[])
    {
        Server server = new Server();
    }

    public void userAdd(String data)
    {
        String message, add = ": : Connect", done = "Server: :Done", name = data;
        ta_chat.append("\nBefore "+name+" added.");
        users.add(name);
        ta_chat.append("\nAfter "+name+" added. ");
        String tempList[] = new String[(users.size())];
        users.toArray(tempList);

        for (String token:tempList)
        {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }

    public void userRemove(String data)
    {
        String message, add = ": : Connect", done = "Server: :Done", name = data;
        users.remove(name);
        String tempList[] = new String[(users.size())];
        users.toArray(tempList);

        for (String token:tempList)
        {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }

    public void tellEveryone(String message)                //Displays message to be sent to clients
    {
        Iterator it = clientOutputStream.iterator();
        while (it.hasNext())
        {
            try
            {
                 writer = (PrintWriter) it.next();
                writer.println(message);
                ta_chat.append("\nSending: "+ message + "");
                writer.flush();
                ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
            }
            catch (Exception ex)
            {
                ta_chat.append("\nError telling everyone. ");
            }
        }
    }

    public class ClientHandler implements Runnable
    {
        BufferedReader reader;
        Socket sock;
        PrintWriter client;

        public ClientHandler(Socket clientSocket, PrintWriter user)
        {
            client = user;
            try
            {
                sock = clientSocket;
                InputStreamReader isReader= new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            }
            catch (Exception ex)
            {
                ta_chat.append("\nUnexpected Error... ");
            }
        }

        @Override
        public void run()
        {
            String message,connect = "Connect", disconnect = "Disconnect", chat = "Chat" ;
            String data[];
            try
            {
                while((message= reader.readLine())!=null)
                {
                    ta_chat.append("\nReceived: "+message+"\n");
                    data = message.split(":");

                    for (String token:data)
                    {
                        ta_chat.append(token+" ");
                    }

                    if(data[2].equals(connect))
                    {
                        tellEveryone((data[0]+":"+data[1]+":"+chat));
                        userAdd(data[0]);
                    }
                    else if (data[2].equals(disconnect))
                    {
                        tellEveryone((data[0]+":has disconnected."+":"+chat));
                        userRemove(data[0]);
                    }
                    else if (data[2].equals(chat))
                    {
                        tellEveryone(message);
                    }
                    else
                    {
                        ta_chat.append("\nNo Conditions were met. ");
                    }
                }
            }
            catch (Exception ex)
            {
                ta_chat.append(" Lost a Connection. ");
                ex.printStackTrace();
                users.remove(client);
            }
        }
    }

    public class ServerStart implements Runnable
    {
        @Override
        public void run()
        {
            users = new ArrayList();
            clientOutputStream = new ArrayList();
            try
            {
                serverSock = new ServerSocket(2222);
               // clientSock = serverSock.accept();
                //Input = new DataInputStream(clientSock.getInputStream());
               // Output = new DataOutputStream(clientSock.getOutputStream());

                while (true)
                {
                    //serverSock=new ServerSocket(2222);
                    clientSock = serverSock.accept();
                    writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStream.add(writer);                                                     //writer from client is added here, thereby only o/p is diplayed on server frame
                    listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    ta_chat.append("\nGot a connection. ");
                }
            }
            catch (IOException e)
                {
                    ta_chat.append("\nError making a connection. ");
                    e.printStackTrace();
                }

        }
    }



    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextArea ta_chat;
    private JButton b_start;
    private JButton b_end;
    private JButton b_users;
    private JButton b_clear;
    private JPanel jPanel;
}