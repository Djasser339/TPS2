package Smart_Farm;

// ==================== INTERFACE SUSPENDABLE ====================

interface Suspendable {
    void suspendre();
    void reactiver();
    boolean estSuspendu();
}

// ==================== INTERFACE ENTITE ====================

interface Entite {
    void ajouterAnimal(Animal a);
}