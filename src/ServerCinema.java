import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCinema {

        ServerSocket socket;
        Socket client_socket; //socket con cui gestiremo i vari clienti

        private int port;

        //questa variabile serve ad identificare il thread associato ad un client
        int client_id;

        //creo un oggetto list, che sarebbe poi un'arraylist
        FilmList list = new FilmList();

        //ciò che avviene inizia qui, nel MAIN
        public static void main(String args[]){

            if(args.length!=1){
                System.out.println("Devi inserire un numero di porta!");
                return;
            }

            //creo l'oggetto server con porta che passo in input nell'array args, in posizione zero
            ServerCinema server = new ServerCinema(Integer.parseInt(args[0]));

            //faccio partire il server (questo metodo è definito fuori dal main più avanti)
            server.start();


        }

        //COSTRUTTORE ServerCinema, al quale dobbiamo passare la porta come parametro
        //Significa che tu non puoi costruire un oggetto se la "private port" non è impostata
        public ServerCinema(int port){
            this.port = port;
        }


        //metodo per INIZIO COMUNICAZIONE CON I CLIENT
        public void start() {
            try {
                //inizializzo un oggetto di tipo socket
                socket=new ServerSocket(port);
                System.out.println("SERVER partito sulla porta selezionata, quindi in ascolto sulla porta:" + port);

                //il while per servire ogni cliente, ogni volta finito il precedente
                boolean servizio = true;
                while (servizio) {
                    client_socket = socket.accept();


                    //appena si collega un client, faccio:
                    //1) CREO LA CLASSE CLIENT MANAGER (passandogli il socket, la lista sulla quale lavorare e l'id)
                    //2) ASSEGNARGLI IL CLIENT PRODOTTO DALL'ULTIMO SOCKET (il client_socket)
                    //3) PRENDERE UN THREAD E ASSEGNARLO A QUESTO CLIENT MANAGER E DO UN NOME AL THREAD
                    //4) FARLO PARTIRE, ed è come chiamare un commesso per affidargli un nuovo cliente
                    //5) INCREMENTARE IL "client_id" PER POTERLO AFFIBBIARE AL PROSSIMO CLIENT
                    ClientManager cm = new ClientManager(client_socket, list, client_id);
                    Thread t = new Thread(cm, "client "+ client_id);
                    t.start();
                    client_id++;

                    //dopo ciò, il server si mette subito in ascolto di nuovo per eventuali altri clienti
                    //mentre i thread (vedili come commessi) servono i client che già si sono collegati


                }


            }catch (IOException e){
                System.out.println("Non puoi far partire il server sulla porta:"+port);
                e.printStackTrace();
            }

        }


    }



