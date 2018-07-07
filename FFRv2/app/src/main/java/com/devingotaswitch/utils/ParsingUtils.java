package com.devingotaswitch.utils;

import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Player;

import java.util.HashMap;
import java.util.Map;

public class ParsingUtils {
    private static Map<String, String> playerFixes;
    private static Map<String, String> teamFixes;

    public static void init() {
        if (playerFixes == null) {
            initHashes();
        }
    }
    private static void initHashes() {
        playerFixes = new HashMap<>();
        teamFixes = new HashMap<>();
        playerFixes.put("Chris D. Johnson", "Chris Johnson");
        playerFixes.put("Charles D. Johnson", "Charles Johnson");
        playerFixes.put("Zach J. Miller", "Zach Miller");
        playerFixes.put("Leveon Bell", "Le'Veon Bell");
        playerFixes.put("LeVeon Bell", "Le'Veon Bell");
        playerFixes.put("Robert Griffin", "Robert Griffin III");
        playerFixes.put("Jonathan Baldwin", "Jon Baldwin");
        playerFixes.put("Mike A. Williams", "Mike Williams");
        playerFixes.put("Ted GinnJr.", "Ted Ginn");
        playerFixes.put("Alex D. Smith", "Alex Smith");
        playerFixes.put("Adrian L. Peterson", "Adrian Peterson");
        playerFixes.put("Robert Housler", "Rob Housler");
        playerFixes.put("Christopher Ivory", "Chris Ivory");
        playerFixes.put("Joseph Morgan", "Joe Morgan");
        playerFixes.put("Devante Parker", "DeVante Parker");
        playerFixes.put("TY Hilton", "T.Y. Hilton");
        playerFixes.put("Ty Hilton", "T.Y. Hilton");
        playerFixes.put("Reuben Randle", "Rueben Randle");
        playerFixes.put("Dori Green-Beckham", "Dorial Green-Beckham");
        playerFixes.put("Michael Crabtree*", "Michael Crabtree");
        playerFixes.put("Malcolm Floyd", "Malcom Floyd");
        playerFixes.put("Terrelle Pryor Sr.", "Terrelle Pryor");
        playerFixes.put("Mike Gilislee", "Mike Gillislee");
        playerFixes.put("LaVon Brazil", "LaVon Brazill");
        playerFixes.put("Steve Hauschka", "Steven Hauschka");
        playerFixes.put("Ben Watson", "Benjamin Watson");
        playerFixes.put("Deangelo Williams", "DeAngelo Williams");
        playerFixes.put("Demarco Murray", "DeMarco Murray");
        playerFixes.put("Laron Byrd", "LaRon Byrd");
        playerFixes.put("Ted Ginn Jr.", "Ted Ginn");
        playerFixes.put("D.J. Williams Jr.", "D.J. Williams");
        playerFixes.put("Greg Jones II", "Greg Jones");
        playerFixes.put("Matt Stafford", "Matthew Stafford");
        playerFixes.put("Jamal Charles", "Jamaal Charles");
        playerFixes.put("CJ Spiller", "C.J. Spiller");
        playerFixes.put("Jon Stewart", "Jonathan Stewart");
        playerFixes.put("Jonathan Franklin", "Johnathan Franklin");
        playerFixes.put("Jaquizz Rodgers", "Jacquizz Rodgers");
        playerFixes.put("AJ Green", "A.J. Green");
        playerFixes.put("Delaine Walker", "Delanie Walker");
        playerFixes.put("Steven Gostkowski", "Stephen Gostkowski");
        playerFixes.put("Cecil Shorts III", "Cecil Shorts");
        playerFixes.put("T Y Hilton", "T.Y. Hilton");
        playerFixes.put("Marvin Jones Jr.", "Marvin Jones");
        playerFixes.put("A Seferian-Jenkins", "Austin Seferian-Jenkins");
        playerFixes.put("Joshua Cribbs", "Josh Cribbs");
        playerFixes.put("Devier Posey", "DeVier Posey");
        playerFixes.put("Robert Griffin, III", "Robert Griffin III");
        playerFixes.put("Le`Veon Bell", "Le'Veon Bell");
        playerFixes.put("Michael Crabtre", "Michael Crabtree");
        playerFixes.put("Zach K. Brown", "Zach Brown");
        playerFixes.put("Gio Bernard", "Giovani Bernard");
        playerFixes.put("Timothy Wright", "Tim Wright");
        playerFixes.put("Terrence Williams", "Terrance Williams");
        playerFixes.put("Odell Beckham Jr.", "Odell Beckham");
        playerFixes.put("Duke Johnson Jr.", "Duke Johnson");
        playerFixes.put("Roy Helu Jr.", "Roy Helu");
        playerFixes.put("Rob Kelley", "Robert Kelley");
        playerFixes.put("Mitch Trubisky", "Mitchell Trubisky");
        playerFixes.put("Will Fuller V", "Will Fuller");

        teamFixes.put("cin", "Cincinnati Bengals");
        teamFixes.put("cincinnati", "Cincinnati Bengals");
        teamFixes.put("bengals", "Cincinnati Bengals");
        teamFixes.put("pit", "Pittsburgh Steelers");
        teamFixes.put("pittsburgh", "Pittsburgh Steelers");
        teamFixes.put("steelers", "Pittsburgh Steelers");
        teamFixes.put("cle", "Cleveland Browns");
        teamFixes.put("clv", "Cleveland Browns");
        teamFixes.put("cleveland", "Cleveland Browns");
        teamFixes.put("browns", "Cleveland Browns");
        teamFixes.put("bal", "Baltimore Ravens");
        teamFixes.put("blt", "Baltimore Ravens");
        teamFixes.put("baltimore", "Baltimore Ravens");
        teamFixes.put("ravens", "Baltimore Ravens");
        teamFixes.put("mia", "Miami Dolphins");
        teamFixes.put("miami", "Miami Dolphins");
        teamFixes.put("dolphins", "Miami Dolphins");
        teamFixes.put("nwe", "New England Patriots");
        teamFixes.put("ne", "New England Patriots");
        teamFixes.put("nep", "New England Patriots");
        teamFixes.put("new england", "New England Patriots");
        teamFixes.put("england", "New England Patriots");
        teamFixes.put("patriots", "New England Patriots");
        teamFixes.put("pats", "New England Patriots");
        teamFixes.put("nyj", "New York Jets");
        teamFixes.put("jets", "New York Jets");
        teamFixes.put("ny jets", "New York Jets");
        teamFixes.put("n.y. jets", "New York Jets");
        teamFixes.put("buf", "Buffalo Bills");
        teamFixes.put("buffalo", "Buffalo Bills");
        teamFixes.put("bills", "Buffalo Bills");
        teamFixes.put("ind", "Indianapolis Colts");
        teamFixes.put("indianapolis", "Indianapolis Colts");
        teamFixes.put("colts", "Indianapolis Colts");
        teamFixes.put("jac", "Jacksonville Jaguars");
        teamFixes.put(" jac", "Jacksonville Jaguars");
        teamFixes.put("jac ", "Jacksonville Jaguars");
        teamFixes.put("jax", "Jacksonville Jaguars");
        teamFixes.put("jacksonville", "Jacksonville Jaguars");
        teamFixes.put("jaguars", "Jacksonville Jaguars");
        teamFixes.put("hou", "Houston Texans");
        teamFixes.put("houston", "Houston Texans");
        teamFixes.put("hst", "Houston Texans");
        teamFixes.put("texans", "Houston Texans");
        teamFixes.put("ten", "Tennessee Titans");
        teamFixes.put("tennessee", "Tennessee Titans");
        teamFixes.put("titans", "Tennessee Titans");
        teamFixes.put("kc", "Kansas City Chiefs");
        teamFixes.put("kcc", "Kansas City Chiefs");
        teamFixes.put("kansas", "Kansas City Chiefs");
        teamFixes.put("kansas city", "Kansas City Chiefs");
        teamFixes.put("chiefs", "Kansas City Chiefs");
        teamFixes.put("oak", "Oakland Raiders");
        teamFixes.put("oakland", "Oakland Raiders");
        teamFixes.put("raiders", "Oakland Raiders");
        teamFixes.put("den", "Denver Broncos");
        teamFixes.put("denver", "Denver Broncos");
        teamFixes.put("broncos", "Denver Broncos");
        teamFixes.put("sd", "Los Angeles Chargers");
        teamFixes.put("lach", "Los Angeles Chargers");
        teamFixes.put("sdc", "Los Angeles Chargers");
        teamFixes.put("san diego", "Los Angeles Chargers");
        teamFixes.put("chargers", "Los Angeles Chargers");
        teamFixes.put("lac", "Los Angeles Chargers");
        teamFixes.put("la chargers", "Los Angeles Chargers");
        teamFixes.put("chi", "Chicago Bears");
        teamFixes.put("chicago", "Chicago Bears");
        teamFixes.put("bears", "Chicago Bears");
        teamFixes.put("min", "Minnesota Vikings");
        teamFixes.put("minnesota", "Minnesota Vikings");
        teamFixes.put("vikings", "Minnesota Vikings");
        teamFixes.put("det", "Detroit Lions");
        teamFixes.put("detroit", "Detroit Lions");
        teamFixes.put("lions", "Detroit Lions");
        teamFixes.put("gb", "Green Bay Packers");
        teamFixes.put("gb", "Green Bay Packers");
        teamFixes.put("gb ", "Green Bay Packers");
        teamFixes.put(" gb", "Green Bay Packers");
        teamFixes.put("gbp", "Green Bay Packers");
        teamFixes.put("green bay", "Green Bay Packers");
        teamFixes.put("packers", "Green Bay Packers");
        teamFixes.put("nyg", "New York Giants");
        teamFixes.put("n.y. giants", "New York Giants");
        teamFixes.put("giants", "New York Giants");
        teamFixes.put("ny giants", "New York Giants");
        teamFixes.put("phi", "Philadelphia Eagles");
        teamFixes.put("philadelphia", "Philadelphia Eagles");
        teamFixes.put("eagles", "Philadelphia Eagles");
        teamFixes.put("dal", "Dallas Cowboys");
        teamFixes.put("dallas", "Dallas Cowboys");
        teamFixes.put("cowboys", "Dallas Cowboys");
        teamFixes.put("was", "Washington Redskins");
        teamFixes.put("wsh", "Washington Redskins");
        teamFixes.put("washington", "Washington Redskins");
        teamFixes.put("redskins", "Washington Redskins");
        teamFixes.put("atl", "Atlanta Falcons");
        teamFixes.put("atlanta", "Atlanta Falcons");
        teamFixes.put("falcons", "Atlanta Falcons");
        teamFixes.put("car", "Carolina Panthers");
        teamFixes.put("carolina", "Carolina Panthers");
        teamFixes.put("panthers", "Carolina Panthers");
        teamFixes.put("no", "New Orleans Saints");
        teamFixes.put("nos", "New Orleans Saints");
        teamFixes.put("new orleans", "New Orleans Saints");
        teamFixes.put("saints", "New Orleans Saints");
        teamFixes.put("bucs", "Tampa Bay Buccaneers");
        teamFixes.put("tb", "Tampa Bay Buccaneers");
        teamFixes.put("tbb", "Tampa Bay Buccaneers");
        teamFixes.put("tampa bay", "Tampa Bay Buccaneers");
        teamFixes.put("buccaneers", "Tampa Bay Buccaneers");
        teamFixes.put("tampa", "Tampa Bay Buccaneers");
        teamFixes.put("sea", "Seattle Seahawks");
        teamFixes.put("seattle", "Seattle Seahawks");
        teamFixes.put("seahawks", "Seattle Seahawks");
        teamFixes.put("sf", "San Francisco 49ers");
        teamFixes.put("sfo", "San Francisco 49ers");
        teamFixes.put("san francisco", "San Francisco 49ers");
        teamFixes.put("ers", "San Francisco 49ers");
        teamFixes.put("49ers", "San Francisco 49ers");
        teamFixes.put("stl", "Los Angeles Rams");
        teamFixes.put("st. louis", "Los Angeles Rams");
        teamFixes.put("st louis", "Los Angeles Rams");
        teamFixes.put("la", "Los Angeles Rams");
        teamFixes.put("ram", "Los Angeles Rams");
        teamFixes.put("larm", "Los Angeles Rams");
        teamFixes.put("rams", "Los Angeles Rams");
        teamFixes.put("sl", "Los Angeles Rams");
        teamFixes.put("la rams", "Los Angeles Rams");
        teamFixes.put("lar", "Los Angeles Rams");
        teamFixes.put("ari", "Arizona Cardinals");
        teamFixes.put("arz", "Arizona Cardinals");
        teamFixes.put("arizona", "Arizona Cardinals");
        teamFixes.put("cardinals", "Arizona Cardinals");
    }

    public static Player normalizePlayerFields(Player player) {
        player.setTeamName(normalizeTeams(player.getTeamName()));
        player.setPosition(normalizePosition(player.getPosition()));
        player.setName(normalizeDefenses(normalizeNames(player.getName())));
        return player;
    }

    public static String normalizeTeams(String team) {
        String low = team.toLowerCase().replaceAll("[^\\x20-\\x7e]", "");
        if (low.split(" ").length > 1
                && (low.split(" ")[1].equals("p") || low.split(" ")[1]
                .equals("q"))) {
            low = low.split(" ")[0];
        }
        if (teamFixes.containsKey(low)) {
            return teamFixes.get(low);
        } else if (low.contains("kansas")) {
            return "Kansas City Chiefs";
        } else if (low.contains("diego")) {
            return "San Diego Chargers";
        } else if (low.contains("green") && low.length() < 6) {
            return "Green Bay Packers";
        } else if (low.contains("tampa")) {
            return "Tampa Bay Buccaneers";
        } else if (low.contains("orleans")) {
            return "New Orleans Saints";
        } else if (low.contains("la rams")) {
            return "St. Louis Rams";
        } else if (low.contains("francisco")) {
            return "San Francisco 49ers";
        } else if (low.contains("england")) {
            return "New England Patriots";
        } else if (low.contains("nyj")) {
            return "New York Jets";
        } else if (low.contains("tb") && ((low.split(" ").length == 1)
                || (low.split(" ").length == 2 && low.split(" ")[0].contains("tb")))) {
            return "Tampa Bay Buccaneers";
        } else if (low.contains("mia") && low.length() < 5) {
            return "Miami Dolphins";
        } else if (low.contains("nyg")) {
            return "New York Giants";
        } else if (low.equals("fa") || low.equals("--") || low.equals("---") || low.equals("wr") || low.equals("")) {
            // All random team strings should go here.
            return Constants.NO_TEAM;
        }
        return team;
    }

    public static String normalizeDefenses(String uName) {
        uName = normalizeTeams(uName);
        String name = uName.toLowerCase();
        if (name.contains("cincinnati")) {
            uName = "Bengals D/ST";
        } else if (name.contains("cleveland") && (name.split(" ").length == 1 || (name.split(" ").length == 2 &&
                name.split(" ")[0].toLowerCase().equals("cleveland")))) {
            uName = "Browns D/ST";
        } else if (name.contains("pittsburgh")) {
            uName = "Steelers D/ST";
        } else if (name.contains("baltimore")) {
            uName = "Ravens D/ST";
        } else if (name.contains("new england")) {
            uName = "Patriots D/ST";
        } else if (name.contains("miami")) {
            uName = "Dolphins D/ST";
        } else if (name.contains("buffalo")) {
            uName = "Bills D/ST";
        } else if (name.contains("new york jets") || name.contains("ny jets")) {
            uName = "Jets D/ST";
        } else if (name.contains("indianapolis")) {
            uName = "Colts D/ST";
        } else if (name.contains("jacksonville")) {
            uName = "Jaguars D/ST";
        } else if (name.contains("houston")) {
            uName = "Texans D/ST";
        } else if (name.contains("tennessee")) {
            uName = "Titans D/ST";
        } else if (name.contains("san diego") || name.contains("los angeles chargers") || name.contains("la chargers") || name.contains("chargers")) {
            uName = "Chargers D/ST";
        } else if (name.contains("kansas city")) {
            uName = "Chiefs D/ST";
        } else if (name.contains("oakland")) {
            uName = "Raiders D/ST";
        } else if (name.contains("denver")) {
            uName = "Broncos D/ST";
        } else if (name.contains("chicago")) {
            uName = "Bears D/ST";
        } else if (name.contains("minnesota")) {
            uName = "Vikings D/ST";
        } else if (name.contains("detroit")) {
            uName = "Lions D/ST";
        } else if (name.contains("green bay")) {
            uName = "Packers D/ST";
        } else if (name.contains("new york giants")
                || name.contains("ny giants")) {
            uName = "Giants D/ST";
        } else if (name.contains("philadelphia")) {
            uName = "Eagles D/ST";
        } else if (name.contains("dallas") && !(name.split(" ").length > 1 && !"dallas cowboys".equals(name))) {
            uName = "Cowboys D/ST";
        } else if (name.contains("washington") && !(name.split(" ").length > 1 && !"washington redskins".equals(name))) {
            uName = "Redskins D/ST";
        } else if (name.contains("new orleans")) {
            uName = "Saints D/ST";
        } else if (name.contains("atlanta")) {
            uName = "Falcons D/ST";
        } else if (name.contains("carolina")) {
            uName = "Panthers D/ST";
        } else if (name.contains("tamba bay") || name.contains("tampa bay")) {
            uName = "Buccaneers D/ST";
        } else if (name.contains("san fran") || name.contains("san francisco")) {
            uName = "49ers D/ST";
        } else if (name.contains("st. louis") || name.contains("st louis") || name.contains("la rams") || name.contains("los angeles rams")) {
            uName = "Rams D/ST";
        } else if (name.contains("arizona")) {
            uName = "Cardinals D/ST";
        } else if (name.contains("seattle")) {
            uName = "Seahawks D/ST";
        }
        return uName;
    }

    public static String normalizeNames(String playerName) {
        if (playerFixes.containsKey(playerName)) {
            playerName = playerFixes.get(playerName);
        } else if (playerName.contains(Constants.DST)) {
            playerName = normalizeDefenses(playerName);
        } else if (playerName.contains("Veon") && playerName.contains("Bell")) {
            playerName = "Le'Veon Bell";
        } else if (playerName.endsWith("II")) {
            playerName = playerName.replace(" II", "");
        } else if (playerName.endsWith("III")) {
            playerName = playerName.replace(" III", "");
        }
        return playerName;
    }

    private static String normalizePosition(String position) {
        switch (position) {
            case "PK":
                position = Constants.K;
                break;
            case "DEF":
            case "DST":
                position = Constants.DST;
                break;
            case "WR,RB":
            case "WR/RB":
            case "RB/WR":
                position = Constants.RB;
                break;
            case "TE,RB":
                position = Constants.TE;
                break;
        }
        return position;
    }

    public static Player conditionallyAddContext(Player oldPlayer, Player newPlayer) {
        oldPlayer.handleNewValue(newPlayer.getAuctionValue());
        newPlayer = normalizePlayerFields(newPlayer);
        if (StringUtils.isBlank(oldPlayer.getTeamName()) && !StringUtils.isBlank(newPlayer.getTeamName())) {
            oldPlayer.setTeamName(newPlayer.getTeamName());
        }
        if (StringUtils.isBlank(oldPlayer.getPosition()) && !StringUtils.isBlank(newPlayer.getPosition())) {
            oldPlayer.setPosition(newPlayer.getPosition());
        }
        if ((oldPlayer.getAge() == null || oldPlayer.getAge() < 18) &&
                (newPlayer.getAge() != null && newPlayer.getAge() > 18)) {
            oldPlayer.setAge(newPlayer.getAge());
        }
        return oldPlayer;
    }

    public static Player getPlayerFromRankings(String name, String team, String pos, double val) {
        Player player = new Player();
        player.setName(name);
        player.setPosition(pos);
        player.setTeamName(team);
        player = normalizePlayerFields(player);
        player.handleNewValue(val);

        return player;
    }
}
