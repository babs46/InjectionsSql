import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

public class TestBasesDeDonnees {
    private static final Logger mysqlLogger = Logger.getLogger("mysqlLogger");
    private static final Logger postgresqlLogger = Logger.getLogger("postgresqlLogger");

    // Configuration de la connexion aux bases de données
    private static final String MYSQL_DB_URL = "jdbc:mysql://localhost:3306/mysql_test";
    private static final String MYSQL_DB_USER = "root";
    private static final String MYSQL_DB_PASSWORD = "0410";

    private static final String POSTGRES_DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRES_DB_USER = "postgres";
    private static final String POSTGRES_DB_PASSWORD = "1234";

    public static void main(String[] args) {

        // Génération des requêtes de test à partir des fichiers texte
        List<String> mysqlQueries = generateTestQueries(mysqlLogger,"C:\\Users\\User\\IdeaProjects\\InjectionsSql\\src\\mysql_test_inputs.txt");
        List<String> postgresQueries = generateTestQueries(postgresqlLogger,"C:\\Users\\User\\IdeaProjects\\InjectionsSql\\src\\postgres_test_inputs.txt");

        // Configuration pour MySQL
        PropertyConfigurator.configure("log4j_mysql.properties");
        Logger mysqlLogger = Logger.getLogger("mysqlLogger");

        // Configuration pour PostgreSQL
        PropertyConfigurator.configure("log4j_postgresql.properties");
        Logger postgresqlLogger = Logger.getLogger("postgresqlLogger");

        // Connexion à la base de données MySQL et exécution des requêtes de test
        try (Connection mysqlConnection = DriverManager.getConnection(MYSQL_DB_URL, MYSQL_DB_USER, MYSQL_DB_PASSWORD)) {
            mysqlLogger.info("Connexion réussie à la base de données MySQL !");
            testInputs(mysqlConnection, mysqlQueries,mysqlLogger);
        } catch (SQLException e) {
            mysqlLogger.error("Erreur lors de la connexion à la base de données MySQL : " + e.getMessage());
            e.printStackTrace();
        }

        // Connexion à la base de données PostgreSQL et exécution des requêtes de test
        try (Connection postgresConnection = DriverManager.getConnection(POSTGRES_DB_URL, POSTGRES_DB_USER, POSTGRES_DB_PASSWORD)) {
            postgresqlLogger.info("Connexion réussie à la base de données PostgreSQL !");
            testInputs(postgresConnection, postgresQueries,postgresqlLogger);
        } catch (SQLException e) {
            postgresqlLogger.error("Erreur lors de la connexion à la base de données PostgreSQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour générer les requêtes de test à partir d'un fichier texte
    private static List<String> generateTestQueries(Logger logger,String filePath) {
        List<String> queries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                queries.add(line);
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier : " + e.getMessage());

            e.printStackTrace();
        }

        return queries;
    }

    // Méthode pour tester les requêtes et récupérer les données
    private static void testInputs(Connection connection, List<String> queries,Logger logger) {
        for (String query : queries) {
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                if (query.trim().toUpperCase().startsWith("SELECT")) {
                    try (ResultSet resultSet = pstmt.executeQuery()) {
                        // Récupérer les métadonnées du ResultSet pour afficher le nom des colonnes
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // Construire l'en-tête avec le nom des colonnes
                        StringBuilder rowData = new StringBuilder();
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                rowData.append("\t| ");
                            }
                            rowData.append(metaData.getColumnName(i));
                        }
                        logger.info("En-tête de la requête : " + query + "\n| " + rowData.toString() + " |");

                        // Construire les lignes de données avec les valeurs des colonnes
                        while (resultSet.next()) {
                            rowData.setLength(0); // Réinitialiser le StringBuilder pour chaque ligne
                            for (int i = 1; i <= columnCount; i++) {
                                if (i > 1) {
                                    rowData.append("\t| ");
                                }
                                rowData.append(resultSet.getString(i));
                            }
                            logger.info("| " + rowData.toString() + " |");
                        }

                        // Afficher une ligne de séparation
                        System.out.print("-".repeat(rowData.length() + 4));
                    }
                } else {
                    // Pour les autres types de requêtes (INSERT, UPDATE, DELETE), nous utilisons executeUpdate
                    int rowsAffected = pstmt.executeUpdate();
                    logger.info("Requête exécutée : " + query + ". Nombre de lignes affectées : " + rowsAffected);

                }
            } catch (SQLException e) {
                logger.error("Erreur lors de l'exécution de la requête : " + query + "\nErreur : " + e.getMessage()+"\n");
                e.printStackTrace();

            }
        }
    }



}
