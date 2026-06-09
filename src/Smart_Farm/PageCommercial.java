package Smart_Farm;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class PageCommercial {

    // =========================================
    // ENTRY POINT
    // =========================================
    public static ScrollPane commercialPage() {
        VBox pageRoot = new VBox(0);
        pageRoot.setFillWidth(true);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("commercial-tabs");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        Tab dashTab    = new Tab("📊  Vue d'ensemble",   buildDashboardContent());
        Tab clientsTab = new Tab("👥  Clients",           buildClientsContent());
        Tab ventesTab  = new Tab("💰  Ventes",            buildVentesContent());

        tabs.getTabs().addAll(dashTab, clientsTab, ventesTab);

        // Refresh dashboard statistics each time the overview tab is selected
        dashTab.setOnSelectionChanged(e -> {
            if (dashTab.isSelected()) {
                CommercialState.refreshDash();
            }
        });

        pageRoot.getChildren().add(tabs);

        ScrollPane outer = new ScrollPane(pageRoot);
        outer.setFitToWidth(true);
        outer.setFitToHeight(true);
        outer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return outer;
    }

    // =========================================
    // TAB 1 — DASHBOARD
    // =========================================
    private static ScrollPane buildDashboardContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(22));
        content.setAlignment(Pos.TOP_CENTER);

        content.getChildren().add(UIFactory.createAnimatedTitle("💼 Vue d'ensemble Commerciale"));

        // ── KPI cards row ──
        HBox kpiRow = buildKpiRow();
        content.getChildren().add(kpiRow);

        // ── Stock disponible ──
        content.getChildren().add(UIFactory.createAnimatedTitle("📦 Stock disponible par zone"));
        VBox stockSection = new VBox(10);
        stockSection.setPadding(new Insets(0, 8, 0, 8));
        Runnable refreshStock = () -> buildStockCards(stockSection);
        refreshStock.run();
        CommercialState.setDashRefresh(refreshStock);
        // also refresh stock when production updates
        ZoneState.nbrZonesProperty().addListener((o, v, n) -> Platform.runLater(refreshStock));
        content.getChildren().add(stockSection);

        // ── Charts row ──
        content.getChildren().add(UIFactory.createAnimatedTitle("📈 Analyses Commerciales"));
        HBox chartsRow = buildChartsRow();
        content.getChildren().add(chartsRow);

        // ── Top clients ──
        content.getChildren().add(UIFactory.createAnimatedTitle("🏆 Top Clients"));
        VBox topClientsBox = new VBox(8);
        topClientsBox.setPadding(new Insets(0, 8, 0, 8));
        Runnable refreshTop = () -> buildTopClientsCards(topClientsBox);
        refreshTop.run();
        CommercialState.nbrVentesProperty().addListener((o, v, n) -> Platform.runLater(refreshTop));
        content.getChildren().add(topClientsBox);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── KPI stat cards ───────────────────────────────────────────────────
    private static HBox buildKpiRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 8, 0, 8));
        row.setAlignment(Pos.CENTER);

        VBox clientsCard = buildStatCard("👥 Clients",
                CommercialState.nbrClientsProperty(), "#1565C0", "#E3F2FD");
        VBox ventesCard  = buildStatCard("💰 Ventes enregistrées",
                CommercialState.nbrVentesProperty(),  "#2E7D32", "#E8F5E9");

        // CA card — DoubleProperty needs special binding
        VBox caCard = new VBox(6);
        caCard.setPadding(new Insets(16, 18, 16, 18));
        caCard.setAlignment(Pos.CENTER_LEFT);
        caCard.setStyle(
                "-fx-background-color: #FFF8E1;" +
                "-fx-background-radius: 14; -fx-border-radius: 14;" +
                "-fx-border-color: #F57F1733; -fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0.1,0,3);");
        HBox.setHgrow(caCard, Priority.ALWAYS);
        Label caTitle = new Label("💵 Chiffre d'affaires");
        caTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #F57F17;");
        Label caVal = new Label();
        caVal.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #F57F17;");
        caVal.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.2f DA", CommercialState.chiffreAffairesProperty().get()),
                CommercialState.chiffreAffairesProperty()));
        caCard.getChildren().addAll(caTitle, caVal);
        UIFactory.applyHoverEffect(caCard);

        row.getChildren().addAll(clientsCard, ventesCard, caCard);
        UIFactory.expandToFill(row);
        return row;
    }

    private static VBox buildStatCard(String title,
                                      javafx.beans.value.ObservableNumberValue value,
                                      String textColor, String bgColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 14; -fx-border-radius: 14;" +
                "-fx-border-color: " + textColor + "33; -fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0.1,0,3);");
        HBox.setHgrow(card, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        Label valueLbl = new Label();
        valueLbl.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        valueLbl.textProperty().bind(
                Bindings.createStringBinding(() -> String.valueOf(value.getValue()), value));
        card.getChildren().addAll(titleLbl, valueLbl);
        UIFactory.applyHoverEffect(card);
        return card;
    }

    // ── Stock disponible cards ────────────────────────────────────────────
    private static void buildStockCards(VBox container) {
        container.getChildren().clear();
        List<String[]> lines = CommercialState.getAvailableStockLines();

        if (lines.isEmpty()) {
            Label none = new Label("Aucune production enregistrée — les données de stock apparaîtront ici dès que la simulation génère des données (toutes les 60 s).");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-style: italic;");
            none.setWrapText(true);
            container.getChildren().add(none);
            return;
        }

        // Group by zone
        Map<String, List<String[]>> byZone = new LinkedHashMap<>();
        for (String[] line : lines) {
            byZone.computeIfAbsent(line[0], k -> new ArrayList<>()).add(line);
        }

        String[] COLORS = {"#1565C0","#2E7D32","#6A1B9A","#BF360C","#00695C","#F57F17"};
        int colorIdx = 0;

        for (Map.Entry<String, List<String[]>> entry : byZone.entrySet()) {
            String zoneName = entry.getKey();
            String color    = COLORS[colorIdx++ % COLORS.length];

            HBox zoneRow = new HBox(12);
            zoneRow.setAlignment(Pos.CENTER_LEFT);
            zoneRow.setPadding(new Insets(8));

            Label zoneLbl = new Label("📍 " + zoneName);
            zoneLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + color + "; -fx-min-width: 140px;");
            zoneRow.getChildren().add(zoneLbl);

            for (String[] line : entry.getValue()) {
                VBox chip = new VBox(2);
                chip.setPadding(new Insets(8, 14, 8, 14));
                chip.setAlignment(Pos.CENTER);
                chip.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                        "-fx-border-color: " + color + "; -fx-border-radius: 10; -fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
                Label typeLbl = new Label(line[1]);
                typeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                Label qtyLbl = new Label(line[2] + " dispo");
                double qty = 0;
                try { qty = Double.parseDouble(line[2]); } catch(Exception ignored) {}
                qtyLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " +
                        (qty <= 0 ? "#c62828" : color) + ";");
                chip.getChildren().addAll(typeLbl, qtyLbl);
                zoneRow.getChildren().add(chip);
            }

            container.getChildren().add(zoneRow);
        }
    }

    // ── Charts row ────────────────────────────────────────────────────────
    private static HBox buildChartsRow() {
        HBox row = new HBox(18);
        row.setPadding(new Insets(0, 8, 0, 8));
        row.setAlignment(Pos.CENTER);

        VBox revCard = buildChartCard("💵 Chiffre d'affaires par produit",  buildRevenueBarChart());
        VBox qtyCard = buildChartCard("📦 Quantités vendues par produit",    buildQuantityBarChart());

        HBox.setHgrow(revCard, Priority.ALWAYS);
        HBox.setHgrow(qtyCard, Priority.ALWAYS);
        row.getChildren().addAll(revCard, qtyCard);
        return row;
    }

    private static BarChart<String, Number> buildRevenueBarChart() {
        BarChart<String, Number> chart = makeBarChart("DA");
        chart.setPrefHeight(220);

        Runnable refresh = () -> {
            chart.getData().clear();
            Map<String, Double> data = CommercialState.getRevenueByProduct();
            if (data.isEmpty()) return;
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            data.forEach((k, v) -> s.getData().add(new XYChart.Data<>(k, v)));
            chart.getData().add(s);
            Platform.runLater(() -> colorBarsSingle(s, "#1565C0"));
        };
        refresh.run();
        CommercialState.nbrVentesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    private static BarChart<String, Number> buildQuantityBarChart() {
        BarChart<String, Number> chart = makeBarChart("Quantité");
        chart.setPrefHeight(220);

        Runnable refresh = () -> {
            chart.getData().clear();
            Map<String, Double> data = CommercialState.getQuantityByProduct();
            if (data.isEmpty()) return;
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            data.forEach((k, v) -> s.getData().add(new XYChart.Data<>(k, v)));
            chart.getData().add(s);
            Platform.runLater(() -> colorBarsSingle(s, "#2E7D32"));
        };
        refresh.run();
        CommercialState.nbrVentesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    // ── Top clients ───────────────────────────────────────────────────────
    private static void buildTopClientsCards(VBox container) {
        container.getChildren().clear();
        List<Client> top = CommercialState.getTopClients(5);
        if (top.isEmpty()) {
            Label none = new Label("Aucune vente enregistrée pour l'instant.");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-style: italic;");
            container.getChildren().add(none);
            return;
        }
        String[] medals = {"🥇","🥈","🥉","4.","5."};
        for (int i = 0; i < top.size(); i++) {
            Client c = top.get(i);
            double ca = CommercialState.getVentesForClient(c.getId())
                    .stream().mapToDouble(Vente::getMontant).sum();
            long nbV = CommercialState.getVentesForClient(c.getId()).size();

            HBox row = new HBox(14);
            row.setPadding(new Insets(10, 18, 10, 18));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
            Label rank   = new Label(medals[i]);
            rank.setStyle("-fx-font-size: 18px;");
            Label name   = new Label(c.getNomComplet());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1565C0;");
            Region sp    = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label ventes = new Label(nbV + " vente" + (nbV > 1 ? "s" : ""));
            ventes.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            Label caLbl  = new Label(String.format("%.2f DA", ca));
            caLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2E7D32;");

            row.getChildren().addAll(rank, name, sp, ventes, caLbl);
            container.getChildren().add(row);
        }
    }

    // =========================================
    // TAB 2 — CLIENTS
    // =========================================
    private static ScrollPane buildClientsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(22));

        content.getChildren().add(UIFactory.createAnimatedTitle("👥 Gestion des Clients"));

        // Action bar
        Button addBtn = UIFactory.createActionButton("➕  Ajouter un client", () -> showAddClientForm(null));
        HBox actionBar = new HBox(addBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(actionBar);

        // Search table
        VBox tableCard = TableFactory.createSearchTableCard(
                List.of("ID", "Nom Complet", "Email", "Téléphone", "Adresse", "Ventes", "CA Total"),
                CommercialState::searchClient,
                CommercialState::mapClient,
                client -> showClientDetail(client),
                refresher -> CommercialState.setClientRefresh(() -> refresher.accept(CommercialState.searchClient("")))
        );
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        content.getChildren().add(tableCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // =========================================
    // TAB 3 — VENTES
    // =========================================
    private static ScrollPane buildVentesContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(22));

        content.getChildren().add(UIFactory.createAnimatedTitle("💰 Gestion des Ventes"));

        Button addBtn = UIFactory.createActionButton("➕  Enregistrer une vente", PageCommercial::showAddVenteForm);
        HBox actionBar = new HBox(addBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(actionBar);

        VBox tableCard = TableFactory.createSearchTableCard(
                List.of("ID", "Client", "Zone", "Produit", "Quantité", "Prix unit.", "Montant", "Date"),
                CommercialState::searchVente,
                CommercialState::mapVente,
                vente -> showVenteDetail(vente),
                refresher -> CommercialState.setVenteRefresh(() -> refresher.accept(CommercialState.searchVente("")))
        );
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        content.getChildren().add(tableCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // =========================================
    // FORM — ADD / EDIT CLIENT
    // =========================================
    private static void showAddClientForm(Client existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Ajouter un client" : "Modifier le client");

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: #f9fdf9;");

        Label title = new Label(existing == null ? "➕ Nouveau Client" : "✏️ Modifier Client");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        TextField nomField      = styledField("Nom *");
        TextField prenomField   = styledField("Prénom *");
        TextField emailField    = styledField("Email");
        TextField telField      = styledField("Téléphone");
        TextField adresseField  = styledField("Adresse");

        if (existing != null) {
            nomField.setText(existing.getNom());
            prenomField.setText(existing.getPrenom());
            emailField.setText(existing.getEmail());
            telField.setText(existing.getTelephone());
            adresseField.setText(existing.getAdresse());
        }

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        Button saveBtn = new Button(existing == null ? "✔  Ajouter" : "✔  Enregistrer");
        saveBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 8 22; -fx-cursor: hand;");
        Button cancelBtn = new Button("✕  Annuler");
        cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #444;" +
                "-fx-background-radius: 8; -fx-padding: 8 22; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String nom    = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            if (nom.isEmpty() || prenom.isEmpty()) {
                errLbl.setText("Le nom et le prénom sont obligatoires.");
                return;
            }
            if (existing == null) {
                Client c = new Client(nom, prenom,
                        emailField.getText().trim(),
                        telField.getText().trim(),
                        adresseField.getText().trim());
                CommercialState.addClient(c);
            } else {
                existing.setNom(nom);
                existing.setPrenom(prenom);
                existing.setEmail(emailField.getText().trim());
                existing.setTelephone(telField.getText().trim());
                existing.setAdresse(adresseField.getText().trim());
                CommercialState.updateClient(existing);
            }
            dialog.close();
        });

        HBox buttons = new HBox(12, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(title,
                fieldRow("Nom *", nomField), fieldRow("Prénom *", prenomField),
                fieldRow("Email", emailField), fieldRow("Téléphone", telField),
                fieldRow("Adresse", adresseField),
                errLbl, buttons);

        Scene scene = new Scene(form, 430, 380);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }

    // =========================================
    // DIALOG — CLIENT DETAIL / HISTORY
    // =========================================
    private static void showClientDetail(Client client) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Client : " + client.getNomComplet());

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f9fdf9;");

        // Header
        Label nameLbl = new Label("👤 " + client.getNomComplet());
        nameLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        Label idLbl = new Label("ID: " + client.getId() + "   |   " +
                client.getEmail() + "   |   " + client.getTelephone());
        idLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // Purchase history table
        Label histTitle = new Label("📜 Historique des achats");
        histTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        List<Vente> history = CommercialState.getVentesForClient(client.getId());
        ScrollPane histTable = TableFactory.createTable(
                List.of("ID Vente", "Zone", "Produit", "Quantité", "Prix unit.", "Montant", "Date"),
                history,
                v -> List.of(v.getId(), v.getZoneNom(), v.getTypeProduction(),
                        String.format("%.2f", v.getQuantite()),
                        String.format("%.2f DA", v.getPrixUnitaire()),
                        String.format("%.2f DA", v.getMontant()),
                        v.getDateVente().toString()),
                null
        );
        histTable.setPrefHeight(200);

        double totalCA = history.stream().mapToDouble(Vente::getMontant).sum();
        Label totalLbl = new Label("Total achats : " + String.format("%.2f DA", totalCA));
        totalLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2E7D32;");

        // Action buttons
        Button editBtn = new Button("✏️  Modifier");
        editBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 7 18; -fx-cursor: hand;");
        editBtn.setOnAction(e -> { dialog.close(); showAddClientForm(client); });

        Button deleteBtn = new Button("🗑  Supprimer");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 7 18; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            if (!CommercialState.deleteClient(client.getId())) {
                showError("Impossible de supprimer ce client : il possède des ventes enregistrées.");
                return;
            }
            dialog.close();
        });

        Button closeBtn = new Button("✕  Fermer");
        closeBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #444;" +
                "-fx-background-radius: 8; -fx-padding: 7 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, editBtn, deleteBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(nameLbl, idLbl, new Separator(), histTitle, histTable, totalLbl, buttons);

        Scene scene = new Scene(root, 700, 440);
        dialog.setScene(scene);
        dialog.show();
    }

    // =========================================
    // FORM — ADD VENTE
    // =========================================
    private static void showAddVenteForm() {
        if (CommercialState.getClients().isEmpty()) {
            showError("Aucun client enregistré. Ajoutez d'abord un client.");
            return;
        }
        List<Zone> zonesWithProduction = ZoneState.getZones().stream()
                .filter(z -> !z.getProductions().isEmpty())
                .collect(Collectors.toList());
        if (zonesWithProduction.isEmpty()) {
            showError("Aucune production disponible.\nLa simulation génère des données toutes les 60 secondes.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Enregistrer une vente");

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: #f9fdf9;");

        Label title = new Label("💰 Nouvelle Vente");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // Client selector
        ComboBox<Client> clientBox = new ComboBox<>();
        clientBox.getItems().addAll(CommercialState.getClients());
        clientBox.setConverter(new StringConverter<>() {
            public String toString(Client c) { return c == null ? "" : c.getNomComplet() + " (" + c.getId() + ")"; }
            public Client fromString(String s) { return null; }
        });
        clientBox.setMaxWidth(Double.MAX_VALUE);
        clientBox.setPromptText("Sélectionner un client…");

        // Zone selector
        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.getItems().addAll(zonesWithProduction);
        zoneBox.setConverter(new StringConverter<>() {
            public String toString(Zone z) { return z == null ? "" : z.getNom() + " (" + z.getType() + ")"; }
            public Zone fromString(String s) { return null; }
        });
        zoneBox.setMaxWidth(Double.MAX_VALUE);
        zoneBox.setPromptText("Sélectionner une zone…");

        // Type production selector (populated when zone selected)
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setPromptText("Sélectionner un type de production…");
        typeBox.setDisable(true);

        // Available quantity display
        Label dispoLbl = new Label("Quantité disponible : —");
        dispoLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        // Quantity & price fields
        TextField qtyField   = styledField("Quantité à vendre *");
        TextField priceField = styledField("Prix unitaire (DA) *");
        TextField notesField = styledField("Notes (optionnel)");

        // When zone changes → repopulate types
        zoneBox.valueProperty().addListener((obs, oldZ, newZ) -> {
            typeBox.getItems().clear();
            typeBox.setDisable(true);
            dispoLbl.setText("Quantité disponible : —");
            if (newZ == null) return;
            List<String> types = CommercialState.getTypesForZone(newZ.getNom());
            if (types.isEmpty()) {
                dispoLbl.setText("Aucune production enregistrée pour cette zone.");
                return;
            }
            typeBox.getItems().addAll(types);
            typeBox.setDisable(false);
        });

        // When type changes → update available qty
        typeBox.valueProperty().addListener((obs, oldT, newT) -> {
            if (newT == null || zoneBox.getValue() == null) { dispoLbl.setText("Quantité disponible : —"); return; }
            double dispo = CommercialState.getQuantiteDisponible(zoneBox.getValue().getNom(), newT);
            String color = dispo <= 0 ? "#c62828" : "#2E7D32";
            dispoLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            dispoLbl.setText(String.format("Quantité disponible : %.2f %s", dispo, newT));
        });

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");
        errLbl.setWrapText(true);

        Button saveBtn = new Button("✔  Enregistrer la vente");
        saveBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 8 22; -fx-cursor: hand;");
        Button cancelBtn = new Button("✕  Annuler");
        cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #444;" +
                "-fx-background-radius: 8; -fx-padding: 8 22; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            errLbl.setText("");
            Client selectedClient = clientBox.getValue();
            Zone   selectedZone   = zoneBox.getValue();
            String selectedType   = typeBox.getValue();

            if (selectedClient == null) { errLbl.setText("Sélectionnez un client."); return; }
            if (selectedZone   == null) { errLbl.setText("Sélectionnez une zone."); return; }
            if (selectedType   == null) { errLbl.setText("Sélectionnez un type de production."); return; }

            double qty = 0, price = 0;
            try { qty   = Double.parseDouble(qtyField.getText().trim().replace(",",".")); }
            catch(NumberFormatException ex) { errLbl.setText("Quantité invalide."); return; }
            try { price = Double.parseDouble(priceField.getText().trim().replace(",",".")); }
            catch(NumberFormatException ex) { errLbl.setText("Prix unitaire invalide."); return; }

            if (qty <= 0)   { errLbl.setText("La quantité doit être positive."); return; }
            if (price <= 0) { errLbl.setText("Le prix doit être positif."); return; }

            double available = CommercialState.getQuantiteDisponible(selectedZone.getNom(), selectedType);
            if (qty > available + 1e-9) {
                errLbl.setText(String.format(
                        "Stock insuffisant ! Disponible : %.2f — Demandé : %.2f", available, qty));
                return;
            }

            Vente v = new Vente(selectedClient.getId(), selectedZone.getNom(),
                    selectedType, qty, price, notesField.getText().trim());
            CommercialState.addVente(v);
            dialog.close();
        });

        HBox buttons = new HBox(12, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(
                title,
                fieldRow("Client *",              clientBox),
                fieldRow("Zone source *",          zoneBox),
                fieldRow("Type de production *",   typeBox),
                dispoLbl,
                fieldRow("Quantité *",             qtyField),
                fieldRow("Prix unitaire (DA) *",   priceField),
                fieldRow("Notes",                  notesField),
                errLbl,
                buttons
        );

        Scene scene = new Scene(form, 480, 500);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }

    // =========================================
    // DIALOG — VENTE DETAIL / DELETE
    // =========================================
    private static void showVenteDetail(Vente vente) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détail vente " + vente.getId());

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f9fdf9;");

        String clientName = CommercialState.getClientById(vente.getClientId())
                .map(Client::getNomComplet).orElse("(inconnu)");

        Label id     = detailRow("ID vente",        vente.getId());
        Label client = detailRow("Client",           clientName);
        Label zone   = detailRow("Zone",             vente.getZoneNom());
        Label prod   = detailRow("Type produit",     vente.getTypeProduction());
        Label qty    = detailRow("Quantité",         String.format("%.2f", vente.getQuantite()));
        Label prix   = detailRow("Prix unitaire",    String.format("%.2f DA", vente.getPrixUnitaire()));
        Label mnt    = detailRow("Montant total",    String.format("%.2f DA", vente.getMontant()));
        Label date   = detailRow("Date",             vente.getDateVente().toString());
        Label notes  = detailRow("Notes",            vente.getNotes().isEmpty() ? "—" : vente.getNotes());

        Label montantBig = new Label(String.format("%.2f DA", vente.getMontant()));
        montantBig.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        Button deleteBtn = new Button("🗑  Supprimer cette vente");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 7 18; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            CommercialState.deleteVente(vente.getId());
            dialog.close();
        });

        Button closeBtn = new Button("✕  Fermer");
        closeBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #444;" +
                "-fx-background-radius: 8; -fx-padding: 7 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, deleteBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                montantBig, new Separator(),
                id, client, zone, prod, qty, prix, mnt, date, notes,
                new Separator(), buttons);

        Scene scene = new Scene(root, 420, 460);
        dialog.setScene(scene);
        dialog.show();
    }

    // =========================================
    // RAPPORT TEXTE (export)
    // =========================================
    static void showRapportCommercial() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Rapport Commercial");

        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("   RAPPORT COMMERCIAL — Smart Farm\n");
        sb.append("==============================\n\n");
        sb.append(String.format("Clients enregistrés : %d%n", CommercialState.getClients().size()));
        sb.append(String.format("Ventes enregistrées : %d%n", CommercialState.getVentes().size()));
        sb.append(String.format("Chiffre d'affaires total : %.2f DA%n%n", CommercialState.getChiffreAffairesTotal()));

        sb.append("── STOCK DISPONIBLE ──────────\n");
        for (String[] line : CommercialState.getAvailableStockLines()) {
            sb.append(String.format("  %-20s %-18s disponible : %s%n", line[0], line[1], line[2]));
        }
        sb.append("\n── CA PAR PRODUIT ────────────\n");
        CommercialState.getRevenueByProduct().forEach((k, v) ->
                sb.append(String.format("  %-20s %.2f DA%n", k, v)));
        sb.append("\n── TOP CLIENTS ───────────────\n");
        List<Client> top = CommercialState.getTopClients(10);
        for (int i = 0; i < top.size(); i++) {
            Client c = top.get(i);
            double ca = CommercialState.getVentesForClient(c.getId())
                    .stream().mapToDouble(Vente::getMontant).sum();
            sb.append(String.format("  %d. %-25s %.2f DA%n", i+1, c.getNomComplet(), ca));
        }
        sb.append("\n── DÉTAIL DES VENTES ─────────\n");
        for (Vente v : CommercialState.getVentes()) {
            String nom = CommercialState.getClientById(v.getClientId())
                    .map(Client::getNomComplet).orElse("?");
            sb.append(String.format("  [%s] %s — %s — %.2f × %.2f DA = %.2f DA  (%s)%n",
                    v.getId(), nom, v.getTypeProduction(),
                    v.getQuantite(), v.getPrixUnitaire(), v.getMontant(), v.getDateVente()));
        }

        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        ta.setPrefSize(650, 480);

        Button closeBtn = new Button("✕  Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        HBox bar = new HBox(closeBtn);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 16, 8, 16));

        BorderPane bp = new BorderPane(ta);
        bp.setBottom(bar);

        dialog.setScene(new Scene(bp, 680, 540));
        dialog.show();
    }

    // =========================================
    // HELPERS — UI
    // =========================================
    private static VBox buildChartCard(String title, javafx.scene.Node chart) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("farm-graph-card");
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("farm-graph-title");
        card.getChildren().addAll(titleLbl, chart);
        return card;
    }

    private static BarChart<String, Number> makeBarChart(String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis   = new NumberAxis();
        yAxis.setLabel(yLabel);
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("farm-bar-chart");
        chart.setCategoryGap(28);
        chart.setBarGap(5);
        return chart;
    }

    private static void colorBarsSingle(XYChart.Series<String, Number> s, String color) {
        for (XYChart.Data<String, Number> d : s.getData()) {
            if (d.getNode() != null)
                d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 8 8 0 0;");
        }
    }

    private static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-font-size: 13px; -fx-padding: 7 10; -fx-background-radius: 7;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private static HBox fieldRow(String label, javafx.scene.Node field) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-min-width: 160px;");
        HBox row = new HBox(10, lbl, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private static Label detailRow(String label, String value) {
        Label l = new Label(label + " :   " + value);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        return l;
    }

    private static void showError(String msg) {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.setTitle("Erreur");
        Label lbl = new Label(msg);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #c62828; -fx-padding: 20;");
        Button ok = new Button("OK");
        ok.setOnAction(e -> s.close());
        VBox vb = new VBox(14, lbl, ok);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(20));
        s.setScene(new Scene(vb, 360, 150));
        s.show();
    }
}
