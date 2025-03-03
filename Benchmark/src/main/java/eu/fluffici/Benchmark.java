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

/*
---------------------------------------------------------------------------------
File Name : Benchmark

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici;

import eu.fluffici.base.TestBase;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Benchmark {
    private final Table table = Table.create("Výsledky benchmarku".toUpperCase());

    public Benchmark() {
        table.addColumns(StringColumn.create("Název testu".toUpperCase()), DoubleColumn.create("Čas provádění (ms)".toUpperCase()), DoubleColumn.create("Načtené objekty".toUpperCase()));
    }

    /**
     * Adds a benchmark to the list of test bases.
     *
     * @param test The test to be added as a benchmark.
     */
    public void addBenchmark(TestBase test) {
        addBenchmarkToTable(test.getName(), test.execute(), table);
    }

    public String print(boolean isDiscord) {
        if (isDiscord) {
            return "```" + this.table.print() + "```";
        } else {
            return this.table.print();
        }
    }

    /**
     * Adds benchmark results to the specified table.
     *
     * @param testName   The name of the test.
     * @param function   The function to execute for the benchmark.
     * @param table      The table to add the benchmark results to.
     */
    private static void addBenchmarkToTable(String testName, Supplier<List<Object>> function, Table table) {
        long startTime = System.nanoTime();

        List<Object> instantiatedClasses = function.get();
        double timeTaken = (System.nanoTime() - startTime) / 1e6;

        table.stringColumn("Název testu".toUpperCase()).append(testName);
        table.doubleColumn("Čas provádění (ms)".toUpperCase()).append(timeTaken);
        table.doubleColumn("Načtené objekty".toUpperCase()).append(instantiatedClasses.size());
    }
}