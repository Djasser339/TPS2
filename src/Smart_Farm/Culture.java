package Smart_Farm;

// ==================== CULTURE ====================

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Culture {
    private FamilleCulture famille;
    private LocalDate datePlantation;
    private LocalDate dateRecolte;
    private StadeCroissance stadeCroissance;
    private Seuil exigencePH;
    private Seuil exigenceHumidite;

    private int temperature;
    private int humidite;
    private int pluviometrie;
    private int pH;
    private int teneurAzote;
    private List<HistoriqueProd> historique = new ArrayList<>();

    static int Nbr;

    public Culture(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                   Seuil exigencePH, Seuil exigenceHumidite) {
        this.famille = famille;
        this.datePlantation = datePlantation;
        this.dateRecolte = dateRecolte;
        this.exigencePH = exigencePH;
        this.exigenceHumidite = exigenceHumidite;
        this.stadeCroissance=StadeCroissance.semis;
        Nbr++;
    }

    public static int getNbr() { return Nbr; }

    public abstract String getTypeCulture();

    public void ajouterHistorique(HistoriqueProd h) { historique.add(h); }
    public List<HistoriqueProd> getHistorique() { return Collections.unmodifiableList(historique); }

    public FamilleCulture getFamille() { return famille; }
    public LocalDate getDatePlantation() { return datePlantation; }
    public LocalDate getDateRecolte() { return dateRecolte; }
    public StadeCroissance getStadeCroissance() { return stadeCroissance; }
    public Seuil getExigencePH() { return exigencePH; }
    public Seuil getExigenceHumidite() { return exigenceHumidite; }
    public int getTemperature() { return temperature; }
    public int getHumidite() { return humidite; }
    public int getPluviometrie() { return pluviometrie; }
    public int getPH() { return pH; }
    public int getTeneurAzote() { return teneurAzote; }

    public void setDateRecolte(LocalDate dateRecolte) { this.dateRecolte = dateRecolte; }
    public void setStadeCroissance(StadeCroissance stadeCroissance) { this.stadeCroissance = stadeCroissance; }
    public void setTemperature(int temperature) { this.temperature = temperature; }
    public void setHumidite(int humidite) { this.humidite = humidite; }
    public void setPluviometrie(int pluviometrie) { this.pluviometrie = pluviometrie; }
    public void setPH(int pH) { this.pH = pH; }
    public void setTeneurAzote(int teneurAzote) { this.teneurAzote = teneurAzote; }

    public String conditionCroissance() {
        return "PH : [ " + exigencePH.getMin() + " , " + exigencePH.getMax() + " ]\n" +
                "Humidité : [ " + exigenceHumidite.getMin() + " , " + exigenceHumidite.getMax() + " ]\n";
    }

    public String afficherStats() {
        return "PH : " + pH + "\n" +
                "Humidité : " + humidite + "\n" +
                "Pluviométrie : " + pluviometrie + "\n" +
                "Température : " + temperature + "\n";
    }

    @Override
    public String toString() {
        return "Culture{" +
                "famille=" + famille +
                ", dateRecolte=" + dateRecolte +
                ", datePlantation=" + datePlantation +
                ", stadeCroissance=" + stadeCroissance +
                '}';
    }
}

class Cereal extends Culture {
    private List<String> cultures = new ArrayList<>();
    static int Nbr;

    public Cereal(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                  Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
        Nbr++;
    }

    public static int getNbr() { return Nbr; }

    @Override
    public String getTypeCulture() { return "Céréale"; }

    public List<String> getCultures() { return cultures; }
    public void addCultures(String culture) { cultures.add(culture); }
    public void removeCultures(String culture) { cultures.remove(culture); }
}

class Legume extends Culture {
    private List<String> cultures = new ArrayList<>();
    static int Nbr;

    public Legume(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                  Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
        Nbr++;
    }

    public static int getNbr() { return Nbr; }

    @Override
    public String getTypeCulture() { return "Légume"; }

    public List<String> getCultures() { return cultures; }
    public void addCultures(String culture) { cultures.add(culture); }
    public void removeCultures(String culture) { cultures.remove(culture); }
}

class Fruit extends Culture {
    private List<String> cultures = new ArrayList<>();
    static int Nbr;

    public Fruit(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                 Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
        Nbr++;
    }

    public static int getNbr() { return Nbr; }

    @Override
    public String getTypeCulture() { return "Fruit"; }

    public List<String> getCultures() { return cultures; }
    public void addCultures(String culture) { cultures.add(culture); }
    public void removeCultures(String culture) { cultures.remove(culture); }
}
