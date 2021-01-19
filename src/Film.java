import java.io.Serializable;
import java.time.LocalTime;

//devo far diventare Film serializable, al fine di poter scrivere correttamente l'Arraylist<Film> su file, ovvero la mia lista
public class Film implements Serializable{

    private String title;
    private String genre;
    private int year;
    private int duration;
    private LocalTime time;

    //costruttore
    public Film(String title, String genre, int year, int duration, LocalTime time) {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.time = time;
    }

    //GETTER E SETTER

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }


    //metodo che mi torna true se un determinato oggetto di tipo film contiene una parola nel suo titolo
    public boolean Title_Contains_KeyWord(String keyword){
        boolean ans = title.contains(keyword);
        return ans;
    }



    //faccio l'Override di to String per avere un metodo che mi torni interamente un oggetto film stampabile sottoforma di stringa
    @Override
    public String toString() {
        return "Film: {" +
                "titolo=" + title +
                ", genere=" + genre +
                ", anno=" + year +
                ", durata=" + duration +
                ", orario=" + time +
                '}';
    }
}
