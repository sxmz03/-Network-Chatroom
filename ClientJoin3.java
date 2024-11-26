package connections;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;


public class ClientJoin3 {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);
    JButton quitButton = new JButton("Quit");

    
    public ClientJoin3(String serverAddress) {
        this.serverAddress = serverAddress;
        
        ///puts together GUI for the chat room
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(quitButton, BorderLayout.EAST);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
        
        ///button from when client wants to leave 
        quitButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		out.println("/quit");
        		frame.dispose();
        	}
        });
    }

    ///chooses unique ID and sends to server
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE
        );
    }
    ///sends IP address to server
    private String getIP() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter your IP",
            "IP address",
            JOptionPane.PLAIN_MESSAGE
        );
    }
    

    private void run(int portNumber) throws IOException {
        try {
        	///connects to server and defines input from user  and output from server
            Socket socket = new Socket(serverAddress, portNumber);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
            	///gets input from server
                String line = in.nextLine();
                ///signal for unique ID
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                    ///signal for user to proceed to server
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                    ///signal for messages sent by other clients
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                    ///signal to check if active on server
                } else if (line.startsWith("IS_ACTIVE")) {out.println("ACTIVE!");
                ///signal for server requesting IP address 
                } else if (line.startsWith("IP") ) {out.println(getIP() + " " + String.valueOf(portNumber));
                } else if (line.startsWith("request")) {}
                
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
    	///get input of server address from user 
    	Scanner addressInput1 = new Scanner(System.in);  
        System.out.println("Enter server address");
        String serverAddress = addressInput1.nextLine();  
        
        ///get port from user 
        Scanner portInput = new Scanner(System.in);
        System.out.println("Enter port number");
        int portNumber = Integer.valueOf(portInput.nextLine());
        
        ///reruns if the IP is not entered
        if (serverAddress.isEmpty()) {
            System.err.println("Address not vaild");
            return;
        }
        ///runs module that connects user to server
        ClientJoin3 client = new ClientJoin3(serverAddress);	
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run(portNumber);
    }
}
