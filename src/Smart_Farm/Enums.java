package Smart_Farm;

// ==================== ÉNUMÉRATIONS ====================

enum StatutCapteur { ACTIVE, INACTIVE, SUSPENDU }

enum TypeMesure {
    TEMPERATURE, HUMIDITE, PLUVIOMETRIE,
    PH_SOL, HUMIDITE_SOL, AZOTE,
    TEMPERATURE_EAU, OXYGENE_DISSOUS, PH_EAU,
    TEMPERATURE_CORPORELLE, ACTIVITE_PAS_PAR_MINUTE
}

enum EtatSante { malade, sain, quarantaine }
enum Gravite { normal, avertissement, critique }
enum TypeZone { aquacole, elevage, culture }
enum StatutZone { ACTIVE, INACTIVE, SUSPENDU }
enum TypeEspece { ruminant, volaille, aqua }
enum StadeCroissance { semis, germination, croissance, maturite, recolte }
enum FamilleCulture { Cereal, Legume, Fruit }
enum TypeZoneElevage { Ruminant, Volaille }
enum TypeEvenSante { VACCIN, MALADIE, GAIN_POIDS }
