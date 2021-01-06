import java.util.Comparator;


//questo un comparatore che utilizzerò per avere il criterio di ordine; ricorda che questa classe implementa l'interfaccia Comparator.
//Con questo comparatore, COMPARERO' GLI ORARI DI PROGRAMMAZIONE
public class TimeComparator implements Comparator<Film> {

    @Override
    public int compare(Film f1, Film f2) {
        return f1.getTime().compareTo(f2.getTime());
    }
}
