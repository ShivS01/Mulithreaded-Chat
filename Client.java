import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
    String username, address = "localhost",message;
    String[] data;
    ArrayList<String> users = new ArrayList();
    int port = 2222;
    Boolean isConnected = false;

    Socket sock=null;
    BufferedReader reader;
    PrintWriter writer;
    JFrame jFrame;

    public Client()
    {
        jFrame = new JFrame("Multithreaded Chat - Client Side");
        jFrame.add(jPanel);
        jFrame.setSize(800,500);
        b_connect.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (isConnected==false)
                {
                    username = tf_username.getText();
                    tf_username.setEditable(false);
                    ta_chat.setText(" ");
                    try
                    {
                        sock = new Socket(address,port);
                        InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
                        reader = new BufferedReader(streamReader);
                        //new IncomingReader().run();
                       /* while((message= reader.readLine())!=null)
                        {
                            ta_chat.append("\nReceived: " + message + "\n");
                            data = message.split(":");
                        }
                        */
                        writer = new PrintWriter(sock.getOutputStream());
                        writer.println(username+":has connected.:Connect");
                        writer.flush();
                        ta_chat.append(username+":has connected.:Connect");
                        isConnected=true;
                    }
                    catch (Exception ex)
                    {
                        ta_chat.append("Cannot Connect! Try Again. \n");
                        tf_username.setEditable(true);
                    }
                    ListenThread();
                }
                else if (isConnected==true)
                {
                    ta_chat.append("\nYou are already connected.\n");
                }
            }
        });
        b_disconnect.addActionListener(new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent e)
           {
               sendDisconnect();
               Disconnect();
           }
        });

        b_anonymous.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                    tf_username.setText("");
                    if (isConnected==false)
                    {
                        String anon="anon";
                        Random generator = new Random();
                        int i = generator.nextInt(999)+1;
                        String is=String.valueOf(i);
                        anon= anon.concat(is);
                        username=anon;

                        tf_username.setText(anon);
                        tf_username.setEditable(false);
                        ta_chat.setText(" ");
                        try
                        {
                            sock = new Socket(address,port);
                            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
                            reader = new BufferedReader(streamReader);
                            writer = new PrintWriter(sock.getOutputStream());
                            writer.println(anon+":has connected.:Connect");
                            ta_chat.append(anon+":has connected.:Connect");
                            writer.flush();
                            isConnected=true;
                        }
                        catch (Exception ex)
                        {
                            ta_chat.append("Cannot Connect! Try Again. \n");
                            tf_username.setEditable(true);
                        }
                        ListenThread();
                    }
                    else if (isConnected==true)
                    {
                        ta_chat.append("You are already connected.\n");
                    }
            }
        });

        b_send.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
                {
                    String nothing = "";
                    if (tf_chat.getText().equals(nothing))
                    {
                        tf_chat.setText("");
                        tf_chat.requestFocus();
                    }
                    else
                    {
                        try
                        {
                            writer.println(username+":"+tf_chat.getText()+":"+"Chat");
                            ta_chat.append(username+":"+tf_chat.getText());
                            writer.flush();
                        }
                        catch (Exception ex)
                        {
                            ta_chat.append("Message was not sent.\n");
                        }
                        tf_chat.setText("");
                        tf_chat.requestFocus();
                    }
                    tf_chat.setText("");
                    tf_chat.requestFocus();
                }
        });

        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    public class IncomingReader implements Runnable                         //check this.......
    {
            public IncomingReader()
            {
                try
                {
                    InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                    reader = new BufferedReader(isReader);
                } catch (Exception ex)
                {
                    ta_chat.append("\nUnexpected Error... ");
                }
            }
            @Override
            public void run()
            {
            String data[];
            String stream, done = " Done ",connect = "Connect", disconnect = "Disconnect", chat = "Chat";

            try
            {
                while ((stream = reader.readLine())!=null)
                {
                    data = stream.split(" : ");
                    if (data[2].equals(chat))
                    {
                        ta_chat.append(data[0]+": "+data[1]+"\n");
                        ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
                    }
                    else if (data[2].equals(connect))
                    {
                        ta_chat.removeAll();
                        userAdd(data[0]);
                    }
                    else if (data[2].equals(disconnect))
                    {
                        userRemove(data[0]);  //userRemove??
                    }
                    else if (data[2].equals(done))
                    {
                        writeUsers();
                        users.clear();
                    }
                }
            }
            catch (Exception ex)
            {
                ta_chat.append("\nUnknown Exception");
            }
        }
    }

    public void ListenThread()
    {
        Thread IncomingReader = new Thread(new IncomingReader());
        IncomingReader.start();
    }

    public void userAdd(String data)
    {
        users.add(data);
    }

    public void userRemove(String data)
    {
        ta_chat.append(data+" is now offline.\n");
        users.remove(data);
    }

    public void writeUsers()
    {
        String tempList[] = new String[(users.size())];
        users.toArray(tempList);
        for (String token:tempList)
        {
            users.add(token+"\n");
        }
    }

    public void sendDisconnect()
    {
        String[] bye = new String[]{username + ": :Disconnect"};
        try
        {
            writer.println("\nbye");
            writer.flush();
        }
        catch (Exception ex)
        {
            ta_chat.append("Could not send Disconnect message.\n");
        }
    }

    public void Disconnect()
    {
        try
        {
            ta_chat.append("\nDisconnected.\n");
            sock.close();
        }
        catch (Exception ex)
        {
            ta_chat.append("Failed to disconnect.\n");
        }
        isConnected = false;
        tf_username.setEditable(true);
    }

    @SuppressWarnings("unchecked")

    public static void main(String args[])
    {
        Client client= new Client();
    }

    private javax.swing.JButton b_anonymous;
    private javax.swing.JButton b_connect;
    private javax.swing.JButton b_send;
    private javax.swing.JButton b_disconnect;
    private javax.swing.JScrollPane jScrollPanel;
    private javax.swing.JLabel lb_address;
    private javax.swing.JLabel lb_password;
    private javax.swing.JLabel lb_port;
    private javax.swing.JLabel lb_username;
    private javax.swing.JTextField tf_address;
    private javax.swing.JTextField tf_chat;
    private javax.swing.JTextField tf_password;
    private javax.swing.JTextField tf_port;
    private javax.swing.JTextField tf_username;
    private JTextArea ta_chat;
    private JPanel jPanel;
}






