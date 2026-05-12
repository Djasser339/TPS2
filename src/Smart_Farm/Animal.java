package Smart_Farm;

// ==================== ANIMAL (ABSTRACT) ====================

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Animal {
    private static int id;
    private String nom;
    private int temperature;
    private int nivActivite;
    private TypeEspece espece;
    private int age;
    private int poid;
    private EtatSante etatSante;
    private CapteurBiometrique capteurBio;
    private CapteurGPS capteurGPS;
    private List<EvenementSante> evenementsSante = new ArrayList<>();

    public Animal(TypeEspece espece, String nom) {
        this.espece = espece;
        this.nom = nom;
        id++;
    }

    public boolean estMalade() { return this.etatSante == EtatSante.malade; }

    public void ajouterEvenementSante(EvenementSante e) { evenementsSante.add(e); }
    public List<EvenementSante> getEvenementsSante() { return Collections.unmodifiableList(evenementsSante); }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getTemperature() { return temperature; }
    public int getNivActivite() { return nivActivite; }
    public TypeEspece getEspece() { return espece; }
    public int getAge() { return age; }
    public int getPoid() { return poid; }
    public EtatSante getEtatSante() { return etatSante; }
    public CapteurBiometrique getCapteurBio() { return capteurBio; }
    public CapteurGPS getCapteurGPS() { return capteurGPS; }

    public void setTemperature(int temperature) { this.temperature = temperature; }
    public void setNivActivite(int nivActivite) { this.nivActivite = nivActivite; }
    public void setAge(int age) { this.age = age; }
    public void setPoid(int poid) { this.poid = poid; }
    public void setEtatSante(EtatSante etatSante) { this.etatSante = etatSante; }
    public void setCapteurBio(CapteurBiometrique capteurBio) { this.capteurBio = capteurBio; }
    public void setCapteurGPS(CapteurGPS capteurGPS) { this.capteurGPS = capteurGPS; }

    public void ajoutterEvenementSante(EvenementSante e) { evenementsSante.add(e); }
    public void SuprimerEvenementSante(EvenementSante e) { evenementsSante.remove(e); }

    @Override
    public String toString() {
        return "Animal{id=" + id + ", nom='" + nom + "', espece=" + espece
                + ", age=" + age + ", poid=" + poid + ", etatSante=" + etatSante
                + ", temperature=" + temperature + "}";
    }
}

class Ruminant extends Animal implements Entite {
    private List<HistoriqueProd> historique = new ArrayList<>();

    public Ruminant( TypeEspece espece, String nom) {
        super( espece, nom);
    }

    @Override
    public void ajouterAnimal(Animal a) {
        System.out.println("Ajout d'un animal associé à " + getNom());
    }

    public void ajouterHistorique(HistoriqueProd p) { historique.add(p); }
    public List<HistoriqueProd> getHistorique() { return Collections.unmodifiableList(historique); }
}

class Volaille extends Animal implements Entite {
    private List<HistoriqueProd> historique = new ArrayList<>();

    public Volaille( TypeEspece espece, String nom) {
        super( espece, nom);
    }

    @Override
    public void ajouterAnimal(Animal a) {
        System.out.println("Ajout d'un animal associé à " + getNom());
    }

    public void ajouterHistorique(HistoriqueProd p) { historique.add(p); }
    public List<HistoriqueProd> getHistorique() { return Collections.unmodifiableList(historique); }
}

class Aquacole extends Animal {
    private List<HistoriqueProd> historique = new ArrayList<>();

    public Aquacole(TypeEspece espece, String nom) {
        super( espece, nom);
    }

    public void ajouterHistorique(HistoriqueProd p) { historique.add(p); }
    public List<HistoriqueProd> getHistorique() { return Collections.unmodifiableList(historique); }
}