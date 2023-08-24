import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class TestBasesDeDonnees {
   private static Logger logger = LogManager.getLogger(TestBasesDeDonnees.class);

    // Configuration de la connexion aux bases de données
    private static final String MYSQL_DB_URL = "jdbc:mysql://localhost:3306/mysql_test";
    private static final String MYSQL_DB_USER = "root";
    private static final String MYSQL_DB_PASSWORD = "0410";

    private static final String POSTGRES_DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRES_DB_USER = "postgres";
    private static final String POSTGRES_DB_PASSWORD = "1234";

    public static void main(String[] args) {
        // Génération des requêtes de test pour les différentes bases de données
        List<String> mysqlQueries = generateMySQLTestQueries();
        List<String> postgresQueries = generatePostgresTestQueries();

        // Connexion à la base de données MySQL et exécution des requêtes de test
        try (Connection mysqlConnection = DriverManager.getConnection(MYSQL_DB_URL, MYSQL_DB_USER, MYSQL_DB_PASSWORD)) {
            logger.info("Connexion réussie à la base de données MySQL !");
            testInputs(mysqlConnection, mysqlQueries);
        } catch (SQLException e) {
            logger.error("Erreur lors de la connexion à la base de données MySQL : " + e.getMessage()+"\n");
            e.printStackTrace();
        }

        // Connexion à la base de données PostgreSQL et exécution des requêtes de test
        try (Connection postgresConnection = DriverManager.getConnection(POSTGRES_DB_URL, POSTGRES_DB_USER, POSTGRES_DB_PASSWORD)) {
            logger.info("Connexion réussie à la base de données PostgreSQL !");
            testInputs(postgresConnection, postgresQueries);
        } catch (SQLException e) {
            logger.error("Erreur lors de la connexion à la base de données PostgreSQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour générer les requêtes de test pour la base de données MySQL
    private static List<String> generateMySQLTestQueries() {
        List<String> queries = new ArrayList<>();

        //queries.add ("SELECT * FROM album WHERE Title = '' OR 1=1 --");
       // queries.add ("SELECT 1/0; OR 1=1;  --");

        //queries.add("SELECT* FROM artist where ArtistId=1;drop table genre");
       // queries.add ("' UNION SELECT null, Username, Password FROM useraccount; --");


        return queries;
    }

    // Méthode pour générer les requêtes de test pour la base de données PostgreSQL
    private static List<String> generatePostgresTestQueries() {
        List<String> queries = new ArrayList<>();
        //queries.add ("SELECT * FROM album WHERE \"Title\" = '' OR 1=1 --");
       // queries.add (" SELECT 1/0;  OR 1=1; --");

        //queries.add (" UNION SELECT null, \"Username\", \"Password\" FROM useraccount; --");
       // queries.add("SELECT* FROM artist where \"ArtistId\" =1;drop table genre");
        return queries;
    }

    // Méthode pour tester les requêtes et récupérer les données
    private static void testInputs(Connection connection, List<String> queries) {
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
                        logger.info("-".repeat(rowData.length() + 4));
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
