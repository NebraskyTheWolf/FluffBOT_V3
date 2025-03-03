package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : DatabaseManager.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import lombok.Getter;
import lombok.Setter;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;
@Getter
@Setter
@SuppressWarnings("All")
public class DatabaseManager {
    public static volatile DatabaseManager instance = null;
    public DataSource dataSource = null;
    private String url;
    private String name;
    private String password;
    private int minPoolSize;
    private int maxPoolSize;

    // Default constructor
    public DatabaseManager(String url, String name, String password, int minPoolSize, int maxPoolSize)
    {
        // Super constructor
        super();
        this.url = url;
        this.name = name;
        this.password = password;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.setupDataSource();
    }

    public synchronized static DatabaseManager getInstance(String url, String name, String password, int minPoolSize, int maxPoolSize)
    {
        if (DatabaseManager.instance == null)
        {
            synchronized(DatabaseManager.class)
            {
                if (DatabaseManager.instance == null)
                {
                    DatabaseManager.instance = new DatabaseManager(url, name, password, minPoolSize, maxPoolSize);
                }
            }
        }
        return DatabaseManager.instance;
    }

    // Initialize the data source
    public void setupDataSource()
    {
        // Set a JDBC/MySQL connection
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.name);
        dataSource.setPassword(this.password);
        dataSource.setInitialSize(this.minPoolSize);
        dataSource.setMaxTotal(this.maxPoolSize);

        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    // Get the data sources stats
    public String getSourcesStats(DataSource dataSource)
    {
        BasicDataSource basicDataSource = (BasicDataSource) dataSource;
        StringBuilder data = new StringBuilder("Number of active: " + basicDataSource.getConnectionInitSqls().size());
        data.append("Number of idle: " + basicDataSource.getNumIdle());
        data.append("================================================================================");

        return data.toString();
    }

    // Shutdown the data source
    @SneakyThrows
    public void shutdownDataSource()
    {
        BasicDataSource basicDataSource = (BasicDataSource) dataSource;
        basicDataSource.close();
    }
}
