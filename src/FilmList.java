import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

//qui in questa nuova classe confeziono il mio Arraylist, in modo da poter definire metodi synchronized
public class FilmList implements Serializable {
        private ArrayList<Film> list;


    //costruttore
    public FilmList(){ list = new ArrayList<Film>();    }




    //METODI da applicare all'arraylist però messi col synchronized così che diversi thread possano lavorarci uno alla volta

    public synchronized void add (Film f){
        list.add(f);
    }


    //questo metodo è per leggere tutti gli elementi dalla lista; creo una lista nuova, che non è altro che una copia della mia lista
    //così che non ho problemi di privacy. non ho la necessità del sinchronized, perchè non è un metodo di modifica
    //creo un altro arraylist


    public ArrayList<Film> showList(){
        return list;
    }

    //questo metodo lo implemento per poter controllare se un film è già stato inserito; se lo è ritorno true alla lista che lo invoca, se no false
    public boolean contains(Film f){
       boolean ans = list.contains(f);
        return ans;
    }


    //metodo per la dimensione di una lista
    public int size() {
        int size = list.size();
        return size;
    }

//metodo analogo all'ADD, ma per la rimozione del film passato come parametro dalla lista che lo chiama
    public synchronized void remove (Film f){
        list.remove(f);

    }


//altro metodo per vedere la lista; fargliela stampare sotto forma di stringa
    @Override
    public String toString() {
        for(Film p:list){
        return "PersonList5{" +
                "list=" + p +
                '}';
    }
        return null;
}


}

