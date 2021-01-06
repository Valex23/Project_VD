import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientCinema {

    //serve solo il socket, senza il Server_socket
    Socket socket;
    private String address;
    private int port;

    //main del client
    public static void main(String args[]){

        if (args.length!=2){
            System.out.println("ADDRESS; necessita' di inserire address e port");
            return;
        }

        //creiamo l'oggetto client, passandogli indirizzo e porta in input (stavolta passo due elementi attraverso args)
        ClientCinema client = new ClientCinema(args[0], Integer.parseInt(args[1]));
        client.start();

    }

    //costruttore client, passandogli indirizzo e porta; analogo al server
    public ClientCinema(String address, int port){
        this.address = address;
        this.port = port;
    }


    //cosa fare quando parte un client
    public void start(){

        //gestione eccezione per scanner e pw
        try {

            socket = new Socket(address,port);
            System.out.println("Iniziata la connessione client all'indirizzo "+address+":"+port);

            //scanner per leggere ciò che immette l'utente, prendendole da tastiera; in particolare è utilizzato per prendere
            //da tastiera la scelta iniziale e per leggere gli input numerici, come anno, durata e sala
            Scanner user_scanner = new Scanner(System.in);

            //-------------------------------------SCANNER ADD-------------------------------------------------------------------------------------
            //scanner per leggere il titolo dell'add, perchè accadeva che utilizzando lo stesso scanner, se il titolo era formato da più
            //stringhe o da stringhe e numeri, si andava in confusione con le altre inserzioni
            Scanner user_scanner_title = new Scanner(System.in);
            //scanner per leggere un'altra stringa, in particolare quella che indica il genere, così da evitare di usare lo stesso scanner
            //per il titolo, che potrebbe essere formato da più parole
            Scanner user_scanner_genre = new Scanner(System.in);
            //-------------------------------------------------------------------------------------------------------------------------------------

            //Print Writer per scrivere la parola da mandare al server
            PrintWriter pw = new PrintWriter(socket.getOutputStream());

            //scanner per leggere le risposte dal server
            Scanner server_scanner = new Scanner(socket.getInputStream());

            //messaggio per rispondere al server
            String msg_to_send;

            //messaggio per leggere quanto inviato dal server
            String msg_received;

            //valore booleano che mi serve per il while che controlla il ciclo padre del programma lato client, CASO GESTORE
            boolean go = false;

            //valore booleano che mi serve per il while che controlla il ciclo padre del programma lato client, CASO CLIENTE NORMALE
            boolean go2 = false;

            //questa stringa di benvenuto la inserisco per l'autenticazione iniziale, prima di qualsiasi menu'
            System.out.println("\nCiao, benvenuti al cinema Vaissela! Procedi all'autenticazione.");
            System.out.println("Inserisci nome: ");
            String name = user_scanner.next().toUpperCase();

            boolean exc=true;
            while (exc){
            System.out.println("\nSei in possesso di una password? ");
            System.out.println("0 - No, voglio continuare come visitatore");
            System.out.println("1 - Si, sono un gestore");

            //try catch per evitare di inserire una stringa anzichè un numero in fase di autenticazione
                try {
                    int authentication = user_scanner.nextInt();

                    while(authentication==1){
                        //sono un gestore



                        System.out.println("\nInserisci password:");
                        int passw = user_scanner.nextInt();
                        if(passw==1){
                            go=true;
                            //per uscire dal ciclo while
                            authentication=2;
                        }
                        else {
                            //ripeto il ciclo
                            System.out.println("Password errata. Sei sicuro di essere un gestore?");
                            System.out.println("0 - No, voglio continuare come visitatore");
                            System.out.println("1 - Si, sono un gestore");
                            authentication = user_scanner.nextInt();
                            //se metto 1 ripeto il ciclo while per inserire la password, se no vado nell'altro if
                        }

                    exc = false;
                    }

                    //sono un cliente normale
                    if(authentication==0) {
                        go2 = true;
                        exc = false;
                    }

                    }catch (InputMismatchException e) {
                    System.out.println("Tentativo fallito! Devi inserire un numero, non una stringa.");
                    //svuoto lo scanner così da poter rifare il procedimento; non svuotandoli mi troverò con lo stesso valore che mi genera l'eccezione
                    //che mi crea un loop infinito.
                    //Posso rifare quindi il procedimento perchè mi trovo dentro un ciclo while; quindi riinizierò da lì, ovvero dal while(exc)
                    user_scanner.nextLine();
                }
            }

            //questa stringa di benvenuto la inserisco fuori per non farla comparire dopo ogni azione scelta
            System.out.println("\nCiao "+name+"! Seleziona un servizio.");

            //variabile che mi servirà nel ciclo del menù per scannerizzare la scelta passata da input
            int choice;

//**************************************************************************************************************************************************************************************
            //finchè ho qualcosa da scrivere e non seleziono il comando QUIT, rimane nel ciclo while
            //CICLO GESTORE
            while (go) {

                    //CREO MENU'

                    System.out.println("------------------------------------------");
                    System.out.println("0 - Aggiungi film");
                    System.out.println("1 - Rimuovi film");
                    System.out.println("2 - Visiona la lista dei film");
                    System.out.println("3 - Salva la lista attuale su file");
                    System.out.println("4 - Carica la lista attuale da file");
                    System.out.println("5 - Chiudi");
                    System.out.println("------------------------------------------");
                    System.out.println("Inserisci il numero corrispondente alla tua scelta-> ");


                    //questo try catch lo utilizzo per gestire la casistica in cui l'utente, erroneamente mette una stringa
                    //anzichè un numero (vale sia per la scelta iniziale "choice", che per i casi successivi di anno e durata.
                    //Questa gestione d'errore è una cosa di cui si occuperà solo il client
                    //il secondo try catch lo faccio per lo stesso motivo, ma riguardo eccezioni in caso di inserzione dell'ORARIO con formato errato

                    try {
                        try {
                        choice = user_scanner.nextInt();

                        //ora metto le casistiche che posso scegliere, con le rispettive cose da inserire per poi mandarle al
                        //server che si occuperà di svolgere le funzioni effettive
                        switch (choice) {
//-------------------------------------------------------------------------------------------------------------------
                            case 0: //ADD

                                //i valori che passo in input di tipo stringhe li rendo tutti maiuscoli per avere uniformità di stile
                                System.out.println("Inserisci il titolo: ");
                                String title = user_scanner_title.nextLine().toUpperCase();
                                //controllo che il campo titolo scansioni un valore e non si lasci il campo vuoto
                                while (title.isEmpty()){
                                     title = user_scanner_title.nextLine().toUpperCase();
                                }

                                System.out.println("Inserisci il genere: ");
                                String genre = user_scanner_genre.nextLine().toUpperCase();
                                //controllo che il campo genere scansioni un valore e non si lasci il campo vuoto
                                while (genre.isEmpty()){
                                    genre = user_scanner_title.nextLine().toUpperCase();
                                }

                                System.out.println("Inserisci l'anno: ");
                                int year = user_scanner.nextInt();
                                System.out.println("Inserisci la durata (in minuti): ");
                                int duration = user_scanner.nextInt();
                                System.out.println("Inserisci l'orario della proiezione(hh:mm): ");
                                LocalTime time = LocalTime.parse(user_scanner.next());

                                //uso un pw.println per ogni campo, perchè qualche campo potrebbe essere composto da più token
                                pw.println("ADD");
                                pw.println(title);
                                pw.println(genre);
                                pw.println(year);
                                pw.println(duration);
                                pw.println(time);
                                pw.flush();

                                //leggo quanto inviatomi dal server
                                msg_received = server_scanner.nextLine();
                                //ora in base a cosa mi risponde il server al mio invio di dati, agisco di conseguenza
                                if (msg_received.equals("ADD_OK")) {
                                    System.out.println("Film aggiunto correttamente!");
                                } else if (msg_received.equals("ALREADY_ADD")) {
                                    System.out.println("Errore nell'aggiunta del film selezionato! Era gia' stato inserito.");
                                } else {
                                    System.out.println("Messaggio sconosciuto!->" + msg_received);
                                }
                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 1: //REMOVE FILM,
                                    //uguale all'ADD
                                System.out.println("Inserisci il titolo del film che vuoi rimuovere: ");
                                String title_r = user_scanner_title.nextLine().toUpperCase();
                                //controllo che il campo titolo scansioni un valore e non si lasci il campo vuoto
                                while (title_r.isEmpty()){
                                    title_r = user_scanner_title.nextLine().toUpperCase();
                                }

                                System.out.println("Inserisci il genere del film che vuoi rimuovere: ");
                                String genre_r = user_scanner_genre.nextLine().toUpperCase();

                                while (genre_r.isEmpty()){
                                    genre_r = user_scanner_title.nextLine().toUpperCase();
                                }
                                System.out.println("Inserisci l'anno del film che vuoi rimuovere: ");
                                int year_r = user_scanner.nextInt();
                                System.out.println("Inserisci la durata (in minuti) del film che vuoi rimuovere: ");
                                int duration_r = user_scanner.nextInt();
                                System.out.println("Inserisci l'orario della proiezione(hh:mm) del film che vuoi rimuovere: ");
                                LocalTime time_r = LocalTime.parse(user_scanner.next());

                                //mando il comando REMOVE
                                pw.println("REMOVE");
                                pw.flush();
                                //leggo quanto inviatomi dal server, perche' la lista può essere vuota o meno. In base al caso gestisco i dati passati in input o meno
                                msg_received = server_scanner.nextLine();
                                if (msg_received.equals("LIST_EMPTY")) {
                                    System.out.println("Lista vuota; non puoi rimuovere nulla!");
                                }
                                else if (msg_received.equals("LIST_FULL")){
                                    //uso un pw.println per ogni campo, perchè qualche campo potrebbe essere composto da più token
                                    pw.println(title_r);
                                    pw.println(genre_r);
                                    pw.println(year_r);
                                    pw.println(duration_r);
                                    pw.println(time_r);
                                    pw.flush();

                                    //ora in base a cosa mi risponde il server al mio invio di dati, agisco di conseguenza
                                    String msg_received2 = server_scanner.nextLine();
                                    if (msg_received2.equals("REMOVE_OK")) {
                                        System.out.println("Film rimosso correttamente!");
                                    } else if (msg_received2.equals("REMOVE_ERR")) {
                                        System.out.println("Errore nella rimozione del film selezionato; questo non e' presente in lista.");
                                    } else {
                                        System.out.println("Messaggio sconosciuto!->" + msg_received);
                                    }
                                }
                                //questo lo dico se ricevo un messaggio sconosciuto con lista vuota o indefinita
                                else {
                                    System.out.println("Messaggio sconosciuto!-> " + msg_received);
                                }

                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 2: //MOSTRA LA LISTA DI TUTTI I FILM
                                System.out.println("\nVuoi stampare la lista completa, oppure vuoi fare una ricerca piu' dettagliata?");
                                System.out.println("------------------------------------------");
                                System.out.println("\t0 - Lista di tutti i film");
                                System.out.println("\t1 - Lista di tutti i film per parola chiave nel titolo");
                                System.out.println("\t2 - Lista di tutti i film per genere");
                                System.out.println("\t3 - Lista di tutti i film per anno");
                                System.out.println("\t4 - Lista di tutti i film per durata");
                                System.out.println("\t5 - Lista di tutti i film per orario");
                                System.out.println("\t6 - Torna al menu' principale");
                                System.out.println("------------------------------------------");
                                System.out.println("Inserisci il numero corrispondente alla tua scelta-> ");


                                int choice2 = user_scanner.nextInt();

                                msg_to_send = "LIST";
                                pw.println(msg_to_send);
                                pw.flush();
                                //leggo quanto inviatomi dal server
                                msg_received = server_scanner.nextLine();

                                //il primo if esterno decreta se la lista è vuota o meno; in caso fosse piena stampo la lista in base alla preferenza dell'utente
                                if (msg_received.equals("QUESTION")) {

                                    //valore booleano che mi servirà a, scorrendo la lista film, capire se un determinato campo, passato in input, è presente in lista
                                    boolean search = true;

                                    switch (choice2) {
                                        //LISTA COMPLETA
                                        case  0:
                                            //mando la mia scelta al server
                                            pw.println("COMPLETE");
                                            pw.flush();
                                            System.out.println("Ricevendo la lista completa...\n");
                                            //stampo la lista completa grazie all'utilizzo di un ciclo che si interrompe non appena non ho più nulla da stampare
                                            boolean listing = true;
                                            while (listing) {
                                                msg_received = server_scanner.nextLine();
                                                if (msg_received.equals("END")) {
                                                    listing = false; //finisco il ciclo
                                                    System.out.println("Fine lista");
                                                } else {
                                                    //se no stampo tutti i film
                                                    System.out.println(msg_received);
                                                }
                                            }
                                            break;
                                    //LISTA PER PAROLA CHIAVE
                                        case 1:
                                            //mando la mia scelta al server
                                            pw.println("KEY_WORD");
                                            pw.flush();
                                            System.out.println("Inserisci la parola chiave per la ricerca del titolo del film:");
                                            String key_word = user_scanner_title.nextLine().toUpperCase();
                                            //mando la mia scelta al server
                                            pw.println(key_word);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("KW_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                                } else if (msg_received.equals("KW_EMPTY")){
                                                    System.out.println("Spiacenti! Nessun film contenente la parola <<"+key_word+">> e' stato trovato.");
                                                }
                                            break;

                                        //LISTA PER GENERE
                                        case 2:
                                            //mando la mia scelta al server
                                            pw.println("GENRE");
                                            pw.flush();
                                            System.out.println("Inserisci il genere:");
                                            String genere = user_scanner_genre.nextLine().toUpperCase();
                                            //mando la mia scelta al server
                                            pw.println(genere);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("GENRE_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("GENRE_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film di genere <<"+genere+">> e' stato trovato.");
                                            }
                                            break;

                                        //LISTA PER ANNO
                                        case 3:
                                            //mando la mia scelta al server
                                            pw.println("YEAR");
                                            pw.flush();
                                            System.out.println("Inserisci l'anno:");
                                            int anno = user_scanner.nextInt();
                                            //mando la mia scelta al server
                                            pw.println(anno);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("YEAR_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("YEAR_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film dell'anno <<"+anno+">> e' stato trovato.");
                                            }
                                            break;
                                        //LISTA PER DURATA
                                        case 4:
                                            //mando la mia scelta al server
                                            pw.println("DURATION");
                                            pw.flush();
                                            System.out.println("Inserisci la durata minima (in minuti):");
                                            int durata = user_scanner.nextInt();
                                            //mando la mia scelta al server
                                            pw.println(durata);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("DURATION_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("DURATION_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film dalla durata di <<"+durata+">> e' stato trovato.");
                                            }
                                            break;
                                        //LISTA PER ORARIO DI PROGRAMMAZIONE
                                        case 5:
                                            //mando la mia scelta al server
                                            pw.println("TIME");
                                            pw.flush();
                                            System.out.println("Inserisci l'orario di programmazione:");
                                            LocalTime orario = LocalTime.parse(user_scanner.next());
                                            //mando la mia scelta al server
                                            pw.println(orario);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("TIME_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("TIME_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film dalle <<"+orario+">> in poi e' stato trovato.");
                                            }
                                            break;
                                        //TORNA AL MENU' PRINCIPALE
                                        case 6:
                                            //mando la mia scelta al server
                                            pw.println("EXIT");
                                            pw.flush();
                                            //metto un semplice break per tornare indietro
                                            System.out.println("...Tornando al menu' principale...");
                                            break;
                                        default:
                                            System.out.println("Devi selezionare un numero tra 0 e 6.");
                                            break;

                                    }

                                }
                                else if(msg_received.equals("EMPTY")){
                                    System.out.println("Lista vuota: tornando al menu' principale...");
                                }
                                else{
                                    System.out.println("Messaggio sconosciuto!->" + msg_received);
                                }
                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 3: //SAVE
                                msg_to_send = "SAVE";
                                pw.println(msg_to_send);
                                pw.flush();

                                //leggo quanto inviatomi dal server
                                msg_received = server_scanner.nextLine();
                                //ora in base a cosa mi risponde il server al mio invio di dati, agisco di conseguenza
                                if (msg_received.equals("SAVE_OK")) {
                                    System.out.println("Salvataggio avvenuto con successo.");
                                } else if (msg_received.equals("LIST_EMPTY")) {
                                    System.out.println("Errore nel salvataggio; lista vuota.");
                                } else if (msg_received.equals("SAVE_ERROR")) {
                                    System.out.println("Errore nel salvataggio.");
                                }else {
                                    System.out.println("Messaggio sconosciuto->" + msg_received);
                                }
                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 4: //LOAD
                                msg_to_send = "LOAD";
                                pw.println(msg_to_send);
                                pw.flush();

                                //leggo quanto inviatomi dal server
                                msg_received = server_scanner.nextLine();
                                //ora in base a cosa mi risponde il server al mio invio di dati, agisco di conseguenza
                                if (msg_received.equals("LOAD_OK")) {
                                    boolean read=true;
                                    while (read) {
                                        String film_found = server_scanner.nextLine();
                                        if (film_found.equals("END")) {
                                            read = false; //finisco il ciclo
                                            System.out.println("Fine file.");
                                        } else {
                                            //se no stampo tutti i film
                                            System.out.println(film_found);
                                        }
                                    }
                                } else if (msg_received.equals("FILE_NOT_FOUND")) {
                                    System.out.println("Errore nel caricamento da file; file non trovato.");
                                } else if (msg_received.equals("LOAD_ERROR")) {
                                    System.out.println("Errore nel caricamento.");
                                }else {
                                    System.out.println("Messaggio sconosciuto->" + msg_received);
                                }
                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 5: //QUIT
                                //setto go a false per uscire dal ciclo che mi fa uscire da tutto, quindi dalla possibilità
                                //di scegliere dal menù
                                go = false;
                                System.out.println("Abbandono della connessione...");
                                msg_to_send = "QUIT";
                                pw.println(msg_to_send);
                                pw.flush();
                                msg_received = server_scanner.nextLine();
                                System.out.println(msg_received);
                                break;

//-------------------------------------------------------------------------------------------------------------------
                            default:System.out.println("Puoi digitare solo un numero compreso tra 0 e 4!");
                            break;
                                        }
//--------------------------------------FINE SWITCH-----------------------------------------------------------------------------

                        } catch (DateTimeParseException d) {
                        System.out.println("\nTentativo fallito! Devi inserire il formato corretto per l'orario (per esempio 20:30).");
                        //svuoto lo scanner così da poter rifare il procedimento; non svuotandoli mi troverò con lo stesso valore che mi genera l'eccezione
                        //posso rifare quindi il procedimento perchè mi trovo dentro un ciclo while; quindi riinizierò da lì, ovvero dal while(go)
                        user_scanner.nextLine();
                    }


                    } catch (InputMismatchException e) {
                        System.out.println("\nTentativo fallito! Devi inserire un numero, non una stringa.");
                        //svuoto lo scanner così da poter rifare il procedimento; non svuotandoli mi troverò con lo stesso valore che mi genera l'eccezione
                        //che mi crea un loop infinito.
                        //Posso rifare quindi il procedimento perchè mi trovo dentro un ciclo while; quindi riinizierò da lì, ovvero dal while(go)
                        user_scanner.nextLine();

                    }

                }
//**************************************************************************************************************************************************************************************
            //CICLO CLIENTE NORMALE
            while (go2) {

                //CREO MENU'

                System.out.println("------------------------------------------");
                System.out.println("0 - Visiona la lista dei film");
                System.out.println("1 - Salva la lista attuale su file");
                System.out.println("2 - Chiudi");
                System.out.println("------------------------------------------");
                System.out.println("Inserisci il numero corrispondente alla tua scelta-> ");

                try {
                    try {
                        choice = user_scanner.nextInt();

                        //ora metto le casistiche che posso scegliere, con le rispettive cose da inserire per poi mandarle al
                        //server per poi svolgere le funzioni effettive
                        switch (choice) {
//-------------------------------------------------------------------------------------------------------------------
                            case 0: //MOSTRA LA LISTA DI TUTTI I FILM
                                System.out.println("\nVuoi stampare la lista completa, oppure vuoi fare una ricerca piu' dettagliata?");
                                System.out.println("------------------------------------------");
                                System.out.println("\t0 - Lista di tutti i film");
                                System.out.println("\t1 - Lista di tutti i film per parola chiave nel titolo");
                                System.out.println("\t2 - Lista di tutti i film per genere");
                                System.out.println("\t3 - Lista di tutti i film per anno");
                                System.out.println("\t4 - Lista di tutti i film per durata");
                                System.out.println("\t5 - Lista di tutti i film per orario");
                                System.out.println("\t6 - Torna al menu' principale");
                                System.out.println("------------------------------------------");
                                System.out.println("Inserisci il numero corrispondente alla tua scelta-> ");


                                int choice2 = user_scanner.nextInt();

                                msg_to_send = "LIST";
                                pw.println(msg_to_send);
                                pw.flush();
                                //leggo quanto inviatomi dal server
                                msg_received = server_scanner.nextLine();

                                //il primo if esterno decreta se la lista è vuota o meno; in caso fosse piena stampo la lista in base alla preferenza dell'utente
                                if (msg_received.equals("QUESTION")) {

                                    //valore booleano che mi servirà a, scorrendo la lista film, capire se un determinato campo, passato in input, è presente in lista
                                    boolean search = true;

                                    //ora metto le casistiche che posso scegliere, con le rispettive cose da inserire per poi mandarle al
                                    //server per poi svolgere le funzioni effettive
                                    switch (choice2) {
                                        //LISTA COMPLETA
                                        case  0:
                                            //mando la mia scelta al server
                                            pw.println("COMPLETE");
                                            pw.flush();
                                            System.out.println("Ricevendo la lista completa...\n");
                                            //stampo la lista completa grazie all'utilizzo di un ciclo che si interrompe non appena non ho più nulla da stampare
                                            boolean listing = true;
                                            while (listing) {
                                                msg_received = server_scanner.nextLine();
                                                if (msg_received.equals("END")) {
                                                    listing = false; //finisco il ciclo
                                                    System.out.println("Fine lista");
                                                } else {
                                                    //se no stampo tutti i film
                                                    System.out.println(msg_received);
                                                }
                                            }
                                            break;
                                        //LISTA PER PAROLA CHIAVE
                                        case 1:
                                            //mando la mia scelta al server
                                            pw.println("KEY_WORD");
                                            pw.flush();
                                            System.out.println("Inserisci la parola chiave per la ricerca del titolo del film:");
                                            String key_word = user_scanner_title.nextLine().toUpperCase();
                                            //mando la mia scelta al server
                                            pw.println(key_word);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("KW_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("KW_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film contenente la parola <<"+key_word+">> e' stato trovato.");
                                            }
                                            break;

                                        //LISTA PER GENERE
                                        case 2:
                                            //mando la mia scelta al server
                                            pw.println("GENRE");
                                            pw.flush();
                                            System.out.println("Inserisci il genere:");
                                            String genere = user_scanner_genre.nextLine().toUpperCase();
                                            //mando la mia scelta al server
                                            pw.println(genere);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("GENRE_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("GENRE_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film di genere <<"+genere+">> e' stato trovato.");
                                            }
                                            break;

                                        //LISTA PER ANNO
                                        case 3:
                                            //mando la mia scelta al server
                                            pw.println("YEAR");
                                            pw.flush();
                                            System.out.println("Inserisci l'anno:");
                                            int anno = user_scanner.nextInt();
                                            //mando la mia scelta al server
                                            pw.println(anno);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("YEAR_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("YEAR_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film dell'anno <<"+anno+">> e' stato trovato.");
                                            }
                                            break;
                                        //LISTA PER DURATA
                                        case 4:
                                            //mando la mia scelta al server
                                            pw.println("DURATION");
                                            pw.flush();
                                            System.out.println("Inserisci la durata minima (in minuti):");
                                            int durata = user_scanner.nextInt();
                                            //mando la mia scelta al server
                                            pw.println(durata);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("DURATION_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("DURATION_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film dalla durata di <<"+durata+">> e' stato trovato.");
                                            }
                                            break;
                                        //LISTA PER ORARIO DI PROGRAMMAZIONE
                                        case 5:
                                            //mando la mia scelta al server
                                            pw.println("TIME");
                                            pw.flush();
                                            System.out.println("Inserisci l'orario di programmazione:");
                                            LocalTime orario = LocalTime.parse(user_scanner.next());
                                            //mando la mia scelta al server
                                            pw.println(orario);
                                            pw.flush();

                                            msg_received = server_scanner.nextLine();

                                            if (msg_received.equals("TIME_FOUND")) {
                                                while (search) {
                                                    String film_found = server_scanner.nextLine();
                                                    if (film_found.equals("END")) {
                                                        search = false; //finisco il ciclo
                                                        System.out.println("Fine lista");
                                                    } else {
                                                        //se no stampo tutti i film
                                                        System.out.println(film_found);
                                                    }
                                                }
                                            } else if (msg_received.equals("TIME_EMPTY")){
                                                System.out.println("Spiacenti! Nessun film alle <<"+orario+">> e' stato trovato.");
                                            }
                                            break;
                                        //TORNA AL MENU' PRINCIPALE
                                        case 6:
                                            //mando la mia scelta al server
                                            pw.println("EXIT");
                                            pw.flush();
                                            //metto un semplice break per tornare indietro
                                            System.out.println("...Tornando al menu' principale...");
                                            break;
                                        default:
                                            System.out.println("Devi selezionare un numero tra 0 e 6.");
                                            break;

                                    }

                                }
                                else if(msg_received.equals("EMPTY")){
                                    System.out.println("Lista vuota!");
                                }
                                else{
                                    System.out.println("Messaggio sconosciuto!" + msg_received);
                                }
                                break;
//-------------------------------------------------------------------------------------------------------------------
                            case 1: //QUIT
                                //setto go2 a false per uscire dal ciclo che mi fa uscire da tutto, quindi dalla possibilità
                                //di scegliere dal menù
                                go2 = false;
                                System.out.println("Abbandono della connessione...");
                                msg_to_send = "QUIT";
                                pw.println(msg_to_send);
                                pw.flush();
                                msg_received = server_scanner.nextLine();
                                System.out.println(msg_received);
                                break;

//-------------------------------------------------------------------------------------------------------------------

                            default:System.out.println("Puoi digitare solo un numero compreso tra 0 e 1!");
                                break;
                        }
//--------------------------------------FINE SWITCH-----------------------------------------------------------------------------

                    } catch (DateTimeParseException d) {
                        System.out.println("\nTentativo fallito! Devi inserire il formato corretto per l'orario (per esempio 20:30).");
                        //svuoto lo scanner così da poter rifare il procedimento; non svuotandoli mi troverò con lo stesso valore che mi genera l'eccezione
                        //posso rifare quindi il procedimento perchè mi trovo dentro un ciclo while; quindi riinizierò da lì, ovvero dal while(go)
                        user_scanner.nextLine();
                    }


                } catch (InputMismatchException e) {
                    System.out.println("\nTentativo fallito! Devi inserire un numero, non una stringa.");
                    //svuoto lo scanner così da poter rifare il procedimento; non svuotandoli mi troverò con lo stesso valore che mi genera l'eccezione
                    //che mi crea un loop infinito.
                    //Posso rifare quindi il procedimento perchè mi trovo dentro un ciclo while; quindi riinizierò da lì, ovvero dal while(go)
                    user_scanner.nextLine();

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
