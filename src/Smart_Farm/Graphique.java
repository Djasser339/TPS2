package Smart_Farm;

// ==================== VISUALISATION GRAPHIQUE ====================

import java.io.Serializable;
import java.util.List;

class Graphique implements Serializable {
    public static void afficherEvolutionReleves(Capteur capteur) {
        List<Releve> releves = capteur.getHistoriqueReleves();
        System.out.println("=== Graphique : Capteur " + capteur.getId() + " ===");
        for (Releve r : releves) {
            String indicateur;
            switch (r.getNiveau()) {
                case critique:      indicateur = "[ROUGE  CRITIQUE     ]"; break;
                case avertissement: indicateur = "[ORANGE AVERTISSEMENT]"; break;
                default:            indicateur = "[VERT   NORMAL       ]"; break;
            }
            System.out.println(indicateur + " " + r.getTimestamp().toLocalTime() + " -> " + r.getValeurAsString());
        }
        System.out.println("==========================================");
    }
}