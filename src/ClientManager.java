import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

//classe che mi creerà nuovo thread
public class ClientManager implements Runnable {

    //premetto che il Client Manager non vede quanto dichiarato dal ServerCinema; non creo l'array sul quale
    //gestirò l'inserzione/estrazione di persone perchè ogni "commesso" andrebbe a creare un array list diverso sul quale lavorare
    //Però dichiaro una variabile list di tipo FilmList (che in realtà è Arraylist<Film>) e poi mi avvalgo del costruttore di ClientManager
    //dove gli passo sia il socket che la lista che usa il server, così che tutti i Client Manager lavorano sulla stessa lista

    //dichiaro il client id così lo metto nel costruttore del client manager; così alla creazione di un Client Manager esso sarà
    //identificato da socket, lista sul quale lavorare, e un id che lo identifica
    private FilmList list;
    private Socket client_socket;
    int client_id;

    //Come detto prima, tramite costruttore passo anche la lista di film, la lista creata dal server e l'id del client
    public ClientManager(Socket myclient, FilmList lista, int c_id) {
        client_socket = myclient;
        list=lista;
        client_id=c_id;
    }

    @Override
    //in questo run, definisco come devo gestire i client che contatteranno il server
    public void run() {
            //metto in una stringa il nome del thread corrente (il nome che praticamente do in fase di inizializzazione)
            String t_id = Thread.currentThread().getName();

            System.out.println("\n"+t_id+"-> Connessione accettata da " + client_socket.getRemoteSocketAddress());


        //Ora stabilire cosa fare; ogni cliente che arriva, in base alla loro richiesta, gli offro un servizio diverso.
        //Come leggo? Attraverso un scanner e poi scrivo con l'InputStream

        //RICORDA: dato che lo scanner e printwriter vogliono un try catch per la gestione di eccezioni, dichiaro esternamente
        //sia lo scanner che il printwriter
        Scanner client_scanner = null;
        PrintWriter pw = null;
        try {
            client_scanner = new Scanner(client_socket.getInputStream());
            pw = new PrintWriter(client_socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


            //un while per soddisfare le varie richieste dello stesso client una volta che prende possesso della comunicazione
            boolean go = true;
            while (go) {

            //metto per iscritto ciò che leggo in input
            String message = client_scanner.nextLine();
            //System.out.println("\nSERVER: ho ricevuto il messaggio " + message+" dal client "+client_id);

            //scannerizzatore del mex che abbiamo ricevuto, attaccato al primo messaggio che ci invia il client;
            //lo facciamo agire sulla stringa per separare i vari blocchi che invia
            Scanner msg_scanner = new Scanner(message);

            //leggo solo il primo token, che sarebbe il comando cmd
            String cmd = msg_scanner.next();
            System.out.println("\nSERVER: Comando ricevuto: " + cmd+", dal client "+client_id);



//--------------------------------------------------------ADD--------------------------------------------------------------------------------------------------------------
            //ciò significa che se ho la parola ADD iniziale nella stringa, fa quanto segue
            if (cmd.equals("ADD")) {

                //riutilizzo lo scanner iniziale, perchè tanto il client mi manda ogni blocco con una divera pw.println
                String title = client_scanner.nextLine();
                String genre = client_scanner.nextLine();
                int year = client_scanner.nextInt();
                int duration = client_scanner.nextInt();
                LocalTime time = LocalTime.parse(client_scanner.next());

                //con quanto appena ottenuto, creo l'oggetto
                Film f = new Film(title, genre, year, duration, time);

                //metodo che mi controlla se la lista contiene già il film che voglio aggiungere e in caso non reinserirlo
                //il metodo è definito al solito, nella classe FilmList che confeziona il mio ArrayList e lo gestisce in tutto.
                //Stesso discorso per il metodo size() chiamato sulla lista; è implementato in FilmList

                //1)CONTROLLO SE LA LISTA E' VUOTA; IN CASO LO FOSSE, AGGIUNGO L'OGGETTO DIRETTAMENTE SENZA FARE CONTROLLI
                //2)SE LA LISTA NON E' VUOTA, CONTROLLO SE L'OGGETTO E' GIA' PRESENTE IN LISTA
                //3)SE E' GIA' PRESENTE, NON VIENE AGGIUNTO NULLA
                //4)SE NON E' PRESENTE, LO AGGIUNGE

                boolean is_in=false;

                //PASSO 1)
                if (list.size()==0){
                    //in realtà questo add() è il metodo della classe FilmList, che coincide con quello dell'aggiungere
                    list.add(f);
                    System.out.println("SERVER LOG: il client "+client_id+" ha fatto aggiungere il " + f);
                    //lo comunico al client che è stato aggiunto;
                    pw.println("ADD_OK");
                }
                //PASSO 2)
                else{
                    for(Film iter:list.showList()){//PASSO 3)
                        if(iter.getTitle().equals(title) && iter.getGenre().equals(genre) && iter.getYear() == (year) && iter.getDuration() == (duration) && iter.getTime().equals(time)){
                            is_in=true;
                            break;
                        }
                    }
                    if (is_in){
                        System.out.println("SERVER LOG: il client "+client_id+" non puo' aggiungere il " +f+ " perche' e' gia' stato inserito!");
                        //lo comunico al client che non è stato aggiunto;
                        pw.println("ALREADY_ADD");
                    }
                    else{
                        list.add(f);
                        System.out.println("SERVER LOG: il client "+client_id+" ha fatto aggiungere il " + f);
                        //lo comunico al client che è stato aggiunto;
                        pw.println("ADD_OK");
                    }

                }
                pw.flush();

//salvo su file automaticamente dopo l'aggiunta
                try {
                    var oos = new ObjectOutputStream(new FileOutputStream("Lista.ser"));
                    //per scrivere la lista su file, l'arraylist seve essere serializable così come lo deve essere il tipo che lo identifica
                    //overo Film; quindi la classe Film deve anch'essa essere serializable
                    oos.writeObject(list);
                    oos.close();
                } catch (IOException e) {
                    pw.println("SAVE_ERROR");
                    pw.flush();
                    e.printStackTrace();
                    System.out.println("SERVER_LOG: errore nel salvataggio della lista");
                }
//fine salvataggio


                //questa azione la faccio per svuotare lo scanner; infatti non facendolo, avevo problemi perchè evidentemente
                //rimaneva qualcosa che veniva preso come comando al ricominciare del ciclo while(go), ovviamente sconosciuto
                client_scanner.nextLine();
            }

//--------------------------------------------------------REMOVE-----------------------------------------------------------------------------------------------------------
            else if (cmd.equals("REMOVE")) {
                    //innanzitutto vedo se la lista è vuota prima di iniziare; se lo è non vado avanti. In ogni caso lo comunico al client
                    if (list.size() == 0) {
                        System.out.println("SERVER LOG: il client " + client_id + " non puo' rimuovere nulla perche' la lista e' vuota");
                        //lo comunico al client
                        pw.println("LIST_EMPTY");
                        pw.flush();
                    }

                    //se la lista non è vuota, proseguo e lo comunico al client
                    else {
                        pw.println("LIST_FULL");
                        pw.flush();

                        String title_r = client_scanner.nextLine();
                        String genre_r = client_scanner.nextLine();
                        int year_r = client_scanner.nextInt();
                        int duration_r = client_scanner.nextInt();
                        LocalTime time_r = LocalTime.parse(client_scanner.next());

                        //passo la lista in una variabile di tipo array list, così da poterla scansionare
                        ArrayList<Film> ll = list.showList();

                        //dichiaro una variabile booleana per lavorare dentro i cicli
                        boolean controllo = false;

                        //scorro nella lista per vedere se c'è un oggetto con i campi del costruttore coincidenti con quelli passati da input; in caso
                        //affermativo rimuove quell'oggetto; poi pongo il controllo pari a true, così da dire al client che ho trovato il film cercato e che lo sto
                        //rimuovendo. Altrimenti pongo controllo pari a false e continuo a scansionare la lista, tanto la variabile controllo rimarrà pari a "false"
                        for (Film iter : ll) {
                            if (iter.getTitle().equals(title_r) && iter.getGenre().equals(genre_r) && iter.getYear() == (year_r) && iter.getDuration() == (duration_r) && iter.getTime().equals(time_r)) {
                                list.remove(iter);
                                controllo = true;
                                //esco subito dal for-each, perchè non mi interessa scansionare più la lista, dato che ho trovato il film che cercavo per eliminarlo
                                break;
                            } else {
                                controllo = false;
                            }
                        }
                        //in base al risultato del ciclo for-each, definisco le due casistiche
                        if ((!controllo)) {
                            System.out.println("SERVER LOG: il client " + client_id + " non puo' rimuovere il film perche' non e' presente in lista!");
                            //lo comunico al client che non è stato rimosso;
                            pw.println("REMOVE_ERR");
                            pw.flush();
                        } else {
                            System.out.println("SERVER LOG: il client " + client_id + " ha fatto rimuovere il Film: {" +
                                    "titolo=" + title_r +
                                    ", genere=" + genre_r +
                                    ", anno=" + year_r +
                                    ", durata=" + duration_r +
                                    ", orario=" + time_r +
                                    '}');
                            //lo comunico al client che è stato rimosso;
                            pw.println("REMOVE_OK");
                            pw.flush();


                            //salvo su file automaticamente dopo la rimozione
                            try {
                                var oos = new ObjectOutputStream(new FileOutputStream("Lista.ser"));
                                //per scrivere la lista su file, l'arraylist seve essere serializable così come lo deve essere il tipo che lo identifica
                                //overo Film; quindi la classe Film deve anch'essa essere serializable
                                oos.writeObject(list);
                                oos.close();
                            } catch (IOException e) {
                                pw.println("SAVE_ERROR");
                                pw.flush();
                                e.printStackTrace();
                                System.out.println("SERVER_LOG: errore nel salvataggio della lista");
                            }
                            //fine salvataggio

                        }

                        //questa azione la faccio per svuotare lo scanner; infatti non facendolo, avevo problemi perchè evidentemente
                        //rimaneva qualcosa che veniva preso come comando al ricominciare del ciclo while(go), ovviamente sconosciuto
                        client_scanner.nextLine();
                    }
            }

//--------------------------------------------------------LIST--------------------------------------------------------------------------------------------------------------
            else if (cmd.equals("LIST")) {
                System.out.println("SERVER LOG: il client "+client_id+" vuole richiedere la lista dei film");


                //***  carica la lista da file
                try {
                    var load = new ObjectInputStream(new FileInputStream("Lista.ser"));
                    Object r = load.readObject();
                    //faccio il cast dell'oggetto
                    FilmList lista = (FilmList) r;
                    load.close();
                    list = lista;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    pw.println("LOAD_ERROR");
                    pw.flush();
                    e.printStackTrace();
                    System.out.println("SERVER_LOG: errore nella lettura della lista");
                }
                //*** proseguo dopo il caricamento da file


                //faccio il controllo se la lista è vuota o meno; in caso fosse vuota lo notifico
                if(list.size()==0){
                    System.out.println("SERVER LOG: lista vuota");
                    pw.println("EMPTY");
                    pw.flush();
                }
                else {
                    pw.println("QUESTION");
                    pw.flush();

                    String answer = client_scanner.next();

                    //passo la lista in una variabile di tipo array list, così da poterla scansionare e successivamente stamparne gli elementi;
                    //questo perchè il for each non funziona su personList, perchè è una classe fatta da me. Allora creo una lista
                    //temporanea e mi chiamo il metodo per vedere/copiare l'originale
                    ArrayList<Film> ll = list.showList();

                    //inizializzo il comparatore che mi stabilisce il criterio di ordine per orario di programmazione
                    TimeComparator tc = new TimeComparator();

                    //ordino la lista secondo il mio comparatore, quindi la lista sarà automaticamente ordinata per orario di programmazione
                    Collections.sort(ll, tc);

                    //valore booleano che mi servirà a, scorrendo la lista film, capire se un determinato campo, passato in input, è presente in lista
                    boolean ok_film = false;

                    switch (answer) {
                        //LISTA COMPLETA
                        case "COMPLETE":
                            //nel frattempo, creo un NUOVO FILM, copiandomi i DATI ESATTI del FILM CHE SCANSIONA; in questo
                            //modo è vero che l'utente legge una copia, ma i dati sono anche MEMORIZZATI IN AREE DIVERSE, perchè
                            //effettivamente sono nuovi oggetti (tutto per QUESTIONI DI SICUREZZA)
                            for (Film f : ll) {
                                Film fc = new Film(f.getTitle(), f.getGenre(), f.getYear(), f.getDuration(), f.getTime());

                                // stampo il nuovo oggetto
                                pw.println(fc);
                                pw.flush();
                            }
                            pw.println("END");
                            pw.flush();
                            System.out.println("SERVER LOG: Lista completa inviata!");
                            break;

                        //LISTA PER PAROLA CHIAVE
                        case "KEY_WORD":
                            //prendo la parola passata in input e mandata dal client
                            String key_word = client_scanner.next();


                            //scorro nella lista per vedere se c'è un oggetto con il titolo contenente quella determinata parola passata in input; in caso
                            //affermativo pongo subito "okfilm" pari a true e passo direttamente al prossimo ciclo tramite il "break". Se no lo pongo a false
                            for (Film f : ll) {
                                if (f.Title_Contains_KeyWord(key_word)) {
                                    ok_film = true;
                                    break;
                                }
                                else ok_film = false;
                            }

                            //in base al valore booleano di ok_film, agisco di conseguenza stampando i film con il titolo contenente quella parola chiave, oppure
                            //dicendo che quel titolo non c'è
                                if (ok_film){
                                    pw.println("KW_FOUND");
                                    pw.flush();

                                    //scorro la lista stampando tutti i film che contengono quella parola chiave
                                    for (Film iter : ll) {
                                        if (iter.Title_Contains_KeyWord(key_word)) {
                                            pw.println(iter);
                                        }
                                    }
                                    System.out.println("SERVER LOG: i film con la parola <<"+key_word+">> vengono inviati al client...");
                                    pw.println("END");
                                    pw.flush();
                                } else {
                                    System.out.println("SERVER LOG: non e' presente nessun film contenente la parola <<"+key_word+">>!");
                                    //lo comunico al client che non è stato trovato;
                                    pw.println("KW_EMPTY");
                                    pw.flush();
                                }
                            break;

                        //LISTA PER GENERE
                        case "GENRE":
                            String genere = client_scanner.next();

                            for (Film f : ll) {
                                if (f.getGenre().contains(genere)) {
                                    ok_film = true;
                                    break;
                                }
                                else ok_film = false;
                            }

                            //in base al valore booleano di ok_film, agisco di conseguenza stampando i film con il genere selezionato dal client, oppure
                            //dicendo che quel film di quel genere non c'è
                            if (ok_film){
                                pw.println("GENRE_FOUND");
                                pw.flush();

                                for (Film iter : ll) {
                                    if (iter.getGenre().contains(genere)) {
                                        pw.println(iter);
                                    }
                                }
                                System.out.println("SERVER LOG: i film di genere <<"+genere+">> vengono inviati al client...");
                                pw.println("END");
                                pw.flush();
                            } else {
                                System.out.println("SERVER LOG: non e' presente nessun film di genere <<"+genere+">>!");
                                //lo comunico al client che non è stato trovato;
                                pw.println("GENRE_EMPTY");
                                pw.flush();
                            }
                            break;

                        //LISTA PER ANNO
                        case "YEAR":
                            int anno = client_scanner.nextInt();

                            for (Film f : ll) {
                                if (f.getYear()==anno) {
                                    ok_film = true;
                                    break;
                                }
                                else ok_film = false;
                            }

                            //in base al valore booleano di ok_film, agisco di conseguenza stampando i film con l'anno selezionato dal client, oppure
                            //dicendo che quel film di quell'anno non c'è
                            if (ok_film){
                                pw.println("YEAR_FOUND");
                                pw.flush();

                                for (Film iter : ll) {
                                    if (iter.getYear()==anno) {
                                        pw.println(iter);
                                    }
                                }
                                System.out.println("SERVER LOG: i film dell'anno <<"+anno+">> vengono inviati al client...");
                                pw.println("END");
                                pw.flush();
                            } else {
                                System.out.println("SERVER LOG: non e' presente nessun film dell'anno <<"+anno+">>!");
                                //lo comunico al client che non è stato trovato;
                                pw.println("YEAR_EMPTY");
                                pw.flush();
                            }
                            break;

                        //LISTA PER DURATA
                        case "DURATION":

                            int durata = client_scanner.nextInt();

                            //inizializzo il comparatore che mi stabilisce il criterio di ordine per durata dei film
                            DurationComparator dc = new DurationComparator();
                            //ordino la lista secondo il mio comparatore, quindi la lista questa volta, sarà automaticamente ordinata per durata dei film
                            Collections.sort(ll, dc);

                            //scorro nella lista per vedere se c'è un oggetto con la durata coincidente con quel determinato numero passato in input (e anche valori superiori);
                            //in caso affermativo pongo subito "okfilm" pari a true e passo direttamente al prossimo ciclo tramite il "break". Se no lo pongo a false
                            for (Film f : ll) {
                                if (f.getDuration() >= durata) {
                                    ok_film = true;
                                    break;
                                }
                                else ok_film = false;
                            }

                            //in base al valore booleano di ok_film, agisco di conseguenza stampando i film con la durata selezionata dal client e anche durata superiore, oppure
                            //dicendo che quel film con quella durata minima non c'è
                            if (ok_film){
                                pw.println("DURATION_FOUND");
                                pw.flush();
                                //scorro la lista stampando tutti i film che contengono quella parola chiave
                                for (Film iter : ll) {
                                    if (iter.getDuration()>=durata) {
                                        pw.println(iter);
                                    }
                                }
                                System.out.println("SERVER LOG: i film dalla durata di almeno <<"+durata+">> minuti vengono inviati al client...");
                                pw.println("END");
                                pw.flush();
                            } else {
                                System.out.println("SERVER LOG: non e' presente nessun film dalla durata di almeno <<"+durata+">> minuti!");
                                //lo comunico al client che non è stato trovato;
                                pw.println("DURATION_EMPTY");
                                pw.flush();
                            }
                            break;

                        //LISTA PER ORARIO DI PROGRAMMAZIONE
                        case "TIME":

                            LocalTime orario = LocalTime.parse(client_scanner.next());


                            //scorro nella lista per vedere se c'è un oggetto che ha la stesso orario di programmazione o un'orario successivo a quella che l'utente passa in input
                            //(diminuito di 1 minuto, cosi che nella ricerca si consideri anche l'orario passato in input, se no considerava solo i successivi all'orario dato);
                            //in caso affermativo pongo subito "okfilm" pari a true e passo direttamente al prossimo ciclo tramite il "break". Se no lo pongo a false
                            for (Film f : ll) {
                                if (f.getTime().isAfter(orario.minus(1, ChronoUnit.MINUTES))) {
                                    ok_film = true;
                                    break;
                                }
                                else ok_film = false;
                            }

                            //in base al valore booleano di ok_film, agisco di conseguenza stampando i film all'orario selezionata dal client(e dopo quell'orario), oppure
                            //dicendo che quel film a quella determinata ora non c'è
                            if (ok_film){
                                pw.println("TIME_FOUND");
                                pw.flush();
                                //scorro la lista stampando tutti i film che contengono quell'ora
                                for (Film iter : ll) {
                                    if (iter.getTime().isAfter(orario.minus(1, ChronoUnit.MINUTES))) {
                                        pw.println(iter);
                                    }
                                }
                                System.out.println("SERVER LOG: i film dalle <<"+orario+">> in poi vengono inviati al client...");
                                pw.println("END");
                                pw.flush();
                            } else {
                                System.out.println("SERVER LOG: non e' presente nessun film dalle <<"+orario+">>!");
                                //lo comunico al client che non è stato trovato;
                                pw.println("TIME_EMPTY");
                                pw.flush();
                            }
                            break;

                        case "EXIT":
                            System.out.println("SERVER LOG: il client ha richiesto di tornare al menu' principale!");
                            break;

                        //questo case verrà utilizzato dal cliente normale, che chiuderà direttamente anzichè tornare al menu' principale
                        case "QUIT":
                            System.out.println("\nSERVER_LOG: il client "+client_id+" vuole chiudere la connessione");
                            System.out.println("SERVER: chiudo la connessione to " + client_socket.getRemoteSocketAddress());
                            pw.println("Il server ha chiuso la connessione, come da protocollo");
                            pw.flush();

                            try {
                                client_socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            go = false; //così non rientro nel while più interno per ascoltare altre richieste dello stesso client
                            break;
                    }

                    //svuoto lo scanner, per lo stesso motivo spiegato nell'ADD (vale per tutti i casi dello switch)
                    client_scanner.nextLine();

                }
            }

//--------------------------------------------------------SAVE--------------------------------------------------------------------------------------------------------------
            else if (cmd.equals("SAVE")) {
                if (list.size() == 0) {
                    System.out.println("SERVER LOG: il client " + client_id + " non puo' salvare nulla perche' la lista e' vuota.");
                    //lo comunico al client
                    pw.println("LIST_EMPTY");
                    pw.flush();
                }
                else {
                    try {
                        String ultima_modifica = new Date().toString();

                        //con quanto segue, aggiungo in coda gli elementi ad una lista già esistente
                        FileWriter fw = new FileWriter("Lista.txt");

                        //scansiono la lista (ritornatami tramite metodo, perchè ricorda che fai riferimento ad una classe FilmList
                        //e non puoi fare il for-each direttamente su list) e man mano ne stampo il contenuto su file
                        for(Film iter:list.showList()) {
                            fw.write(iter.toString()+""+ultima_modifica+"\n");
                        }
                        fw.close();

                        pw.println("SAVE_OK");
                        pw.flush();
                        System.out.println("SERVER_LOG: lista salvata correttamente!");

                    } catch (IOException e) {
                        pw.println("SAVE_ERROR");
                        pw.flush();
                        e.printStackTrace();
                        System.out.println("SERVER_LOG: errore nel salvataggio della lista");
                    }
                }
            }

//--------------------------------------------------------LOAD--------------------------------------------------------------------------------------------------------------
            else if (cmd.equals("LOAD")) {
                try {
                    try {

                        var load = new ObjectInputStream(new FileInputStream("Lista.ser"));

                        Object r = load.readObject();

                        //faccio il cast dell'oggetto
                        FilmList lista = (FilmList) r;

                        load.close();

                        list = lista;

                        //se arrivo qui senza errori, è perchè il file esiste e posso fare il caricamento (ecco perchè i try-catch)
                        pw.println("LOAD_OK");
                        //pw.println(lista);
                        pw.flush();
                        System.out.println("SERVER_LOG: lista prelevata correttamente!");

                    } catch (FileNotFoundException n) {
                        System.out.println("SERVER LOG: il client " + client_id + " non puo' caricare nulla perche' il file non e' stato trovato");
                        //lo comunico al client
                        pw.println("FILE_NOT_FOUND");
                        pw.flush();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    pw.println("LOAD_ERROR");
                    pw.flush();
                    e.printStackTrace();
                    System.out.println("SERVER_LOG: errore nella lettura della lista");
                }
            }

//--------------------------------------------------------QUIT--------------------------------------------------------------------------------------------------------------
            //nel mio protocollo voglio che preveda anche che se riceve la parola QUIT, chiudo il socket con il client in considerazione
            else if (message.equals("QUIT")) {
                System.out.println("SERVER_LOG: il client "+client_id+" vuole chiudere la connessione");
                System.out.println("SERVER: chiudo la connessione to " + client_socket.getRemoteSocketAddress());

                //questo mex di risposta è fondamentale, perchè il client aspetta sempre una stringa di risposta,
                // anche per il QUIT; altrimenti va in errore
                pw.println("Il server ha chiuso la connessione, come da protocollo");
                pw.flush();

                try {
                    client_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                go = false; //così non rientro nel while più interno per ascoltare altre richieste dello stesso client
            }


            //se il messaggio non rientra nelle precedenti casistiche, stampo ciò
            else {
                System.out.println(message + " è un comando sconosciuto!");
                pw.println("ERROR_COMMAND"); //da mandare al client, per notificarglielo
                pw.flush();
            }


        }

    }
}
