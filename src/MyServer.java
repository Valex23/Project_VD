import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MyServer {

    ServerSocket socket;
    Socket client_socket; //socket con cui gestiremo i vari clienti

    private int port;

 //ciò che avviene inizia qui
    public static void main(String args[]){

        if(args.length!=1){
            System.out.println("Usage java MyServer<port>");
            return;
        }

        //creo l'oggetto server con porta che passo in input nell'array args, in posizione zero
        MyServer server = new MyServer(Integer.parseInt(args[0]));

        //faccio partire il server
        server.start();

    }

    //COSTRUTTORE MyServer, al quale dobbiamo passare la porta come parametro
    //Significa che tu non puoi costruire un oggetto se la "private port" non è impostata
    public MyServer(int port){
        //qui dentro potremmo mettere tutti i casi di selezione porta, in base
        //al valore che gli diamo
        System.out.println("Inizializzo MyServer con porta:" + port);
        this.port = port;
    }


    //metodo per inizio comunicazione
    public void start() {
        try {
            System.out.println("Server in caricamento per partire sulla porta:" + port);
            socket=new ServerSocket(port);
            System.out.println("Server partito sulla porta:" + port);

            //il while per servire ogni cliente, ogni volta finito il precedente
            while (true) {
                System.out.println("In ascolto su porta:" + port);
                client_socket = socket.accept();
                System.out.println("Connessione accettata da " + client_socket.getRemoteSocketAddress());

                //Ora stabilire cosa fare; ogni cliente che arriva, in base alla loro richiesta,
                // gli offro un servizio diverso (appena mi dici una cosa, te la trasformo in lettere maiuscole
                // e te la rimando indietro). COme leggo? Attraverso un inputStream o scanner e poi scrivo con l'InputStream
                Scanner client_scanner = new Scanner(client_socket.getInputStream());
                PrintWriter pw = new PrintWriter(client_socket.getOutputStream());

                //un altro while per soddisfare le varie richieste dello stesso client una volta che prende possesso della comunicazione
                boolean go=true;
                while (go){

                    //metto per iscritto ciò che leggo in input
                    String message = client_scanner.nextLine();
                    System.out.println("Server: ho ricevuto il messaggio: "+message);

                    //ora lo rendo maiuscolo questo mex e lo mando al client
                    String message_big= message.toUpperCase();
                    System.out.println("Server: Sto mandando "+message_big + " to "+client_socket.getRemoteSocketAddress());
                    pw.println(message_big);
                    pw.flush(); //CHIUDERE SEMPRE IL PW

                    //il mio protocollo voglio che preveda anche che se riceve la parola QUIT, chiudo il socket con il client in considerazione
                    if(message.equals("QUIT")){
                        System.out.println("Server: chiudo la connessione to "+client_socket.getRemoteSocketAddress());
                        client_socket.close();
                        go=false; //così non rientro nel while più interno per ascoltare altre richieste dello stesso client, e il server si rimette
                        //in ascolto di altri client
                    }

                }

            }
            //socket.close(); questo in caso volessi chiudere il socket, ma non il server


        }catch (IOException e){
            System.out.println("Non puoi far partire il server sulla porta:"+port);
            e.printStackTrace();
        }

    }
}
