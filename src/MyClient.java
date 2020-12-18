import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {

    //serve solo il socket, senza il Server_socket
    Socket socket;
    private String address;
    private int port;

    //main del client
    public static void main(String args[]){

        if (args.length!=2){
            System.out.println(" Usage: java Client necessita di address e port");
            return;
        }

        //creiamo l'oggetto client, passandogli indirizzo e porta in input (stavolta passo due elementi attraverso args)
        MyClient client = new MyClient(args[0], Integer.parseInt(args[1]));
        client.start();

    }

    //costruttore client, passandogli indirizzo e porta; analogo al server
    public MyClient (String address, int port){
        this.address = address;
        this.port = port;
    }

    //cosa fare quando parte un client
    public void start(){

        System.out.println("Inizia la connessione del client to "+address+":"+port);

        try {

            socket = new Socket(address,port);
            System.out.println("Iniziata la connessione client to "+address+":"+port);

            //scanner per leggere dall'utente, prendendole da tastiera
            Scanner user_scanner = new Scanner(System.in);
            //Print Writer per scrivere la parola da mandare al server
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            //scanner per leggere le parole in maiuscolo, quindi le risposte dal server
            Scanner scanner = new Scanner(socket.getInputStream());


            //solita cosa fatta anche nel server, finchè ho qualcosa da scrivere e non scrivo la parola QUIT,
            // rimane nel ciclo while
            boolean go = true;
            while (go){

                System.out.println("Inserisci una stringa da inviare");
                String message_to_send = user_scanner.nextLine();
                System.out.println("Inviando "+message_to_send);

                //invio al server il messaggio digitato da tastiera
                pw.println(message_to_send);
                pw.flush();

                //scannerizzo il messaggio di risposta del server
                String received_message = scanner.nextLine();
                System.out.println("Ho ricevuto il messaggio: "+ received_message);

                //client attacca uno scanner alla stringa ricevuta, come fatto nel Server3 con cmd
                //così il client legge anche ciò che ho oltre il semplice "ADD_OK"
                Scanner rcv_msg_scanner = new Scanner(received_message);

                String read = rcv_msg_scanner.next();


                //caso in cui mando QUIT, dopo il quale so che il server chiude il socket con il client in considerazione
                if (message_to_send.equals("QUIT")){
                    System.out.println("Chiudendo la connessione to "+ socket.getRemoteSocketAddress());

                    //chiudo, come da protocollo stabilito con il server
                    socket.close();
                    go = false;
                }

                if (message_to_send.equals("CLOSE")){
                    System.out.println("Chiudendo la connessione to "+ socket.getRemoteSocketAddress());

                    //chiudo, come da protocollo stabilito con il server
                    socket.close();
                    go = false;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
