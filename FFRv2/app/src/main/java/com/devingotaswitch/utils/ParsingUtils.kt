package com.devingotaswitch.utils

import android.util.Log
import com.amazonaws.util.StringUtils
import com.devingotaswitch.rankings.domain.Player
import java.util.*
import kotlin.collections.HashMap

object ParsingUtils {
    private var playerFixes: MutableMap<String, String> = HashMap()
    private var teamFixes: MutableMap<String, String> = HashMap()
    @JvmStatic
    fun init() {
        if (playerFixes.isEmpty()) {
            initHashes()
        }
    }

    private fun initHashes() {
        playerFixes["Charles D. Johnson"] = "Charles Johnson"
        playerFixes["DJ Chark Jr."] = "D.J. Chark"
        playerFixes["Henry Ruggsl"] = "Henry Ruggs"
        playerFixes["Henry RuggsI"] = "Henry Ruggs"
        playerFixes["Henry Ruggs III"] = "Henry Ruggs"
        playerFixes["Leveon Bell"] = "Le'Veon Bell"
        playerFixes["LeVeon Bell"] = "Le'Veon Bell"
        playerFixes["DJ Moore"] = "D.J. Moore"
        playerFixes["Dj Moore"] = "D.J. Moore"
        playerFixes["Robert Griffin"] = "Robert Griffin III"
        playerFixes["Robert GriffinI"] = "Robert Griffin III"
        playerFixes["Jonathan Baldwin"] = "Jon Baldwin"
        playerFixes["Mike A. Williams"] = "Mike Williams"
        playerFixes["Ted GinnJr."] = "Ted Ginn"
        playerFixes["Alex D. Smith"] = "Alex Smith"
        playerFixes["Adrian L. Peterson"] = "Adrian Peterson"
        playerFixes["Robert Housler"] = "Rob Housler"
        playerFixes["Christopher Ivory"] = "Chris Ivory"
        playerFixes["Chris Herndon IV"] = "Chris Herndon"
        playerFixes["Devante Parker"] = "DeVante Parker"
        playerFixes["TY Hilton"] = "T.Y. Hilton"
        playerFixes["Ty Hilton"] = "T.Y. Hilton"
        playerFixes["Reuben Randle"] = "Rueben Randle"
        playerFixes["Michael Crabtree*"] = "Michael Crabtree"
        playerFixes["Terrelle Pryor Sr."] = "Terrelle Pryor"
        playerFixes["Mike Gilislee"] = "Mike Gillislee"
        playerFixes["LaVon Brazil"] = "LaVon Brazill"
        playerFixes["Steve Hauschka"] = "Steven Hauschka"
        playerFixes["Ben Watson"] = "Benjamin Watson"
        playerFixes["Laron Byrd"] = "LaRon Byrd"
        playerFixes["Ted Ginn Jr."] = "Ted Ginn"
        playerFixes["D.J. Williams Jr."] = "D.J. Williams"
        playerFixes["Greg Jones II"] = "Greg Jones"
        playerFixes["Matt Stafford"] = "Matthew Stafford"
        playerFixes["Jon Stewart"] = "Jonathan Stewart"
        playerFixes["Jonathan Franklin"] = "Johnathan Franklin"
        playerFixes["Jaquizz Rodgers"] = "Jacquizz Rodgers"
        playerFixes["AJ Green"] = "A.J. Green"
        playerFixes["Delaine Walker"] = "Delanie Walker"
        playerFixes["Steven Gostkowski"] = "Stephen Gostkowski"
        playerFixes["T Y Hilton"] = "T.Y. Hilton"
        playerFixes["Marvin Jones Jr."] = "Marvin Jones"
        playerFixes["A Seferian-Jenkins"] = "Austin Seferian-Jenkins"
        playerFixes["Devier Posey"] = "DeVier Posey"
        playerFixes["Robert Griffin, III"] = "Robert Griffin III"
        playerFixes["Le`Veon Bell"] = "Le'Veon Bell"
        playerFixes["Michael Crabtre"] = "Michael Crabtree"
        playerFixes["Zach K. Brown"] = "Zach Brown"
        playerFixes["Gio Bernard"] = "Giovani Bernard"
        playerFixes["Timothy Wright"] = "Tim Wright"
        playerFixes["Terrence Williams"] = "Terrance Williams"
        playerFixes["Odell Beckham Jr."] = "Odell Beckham"
        playerFixes["Duke Johnson Jr."] = "Duke Johnson"
        playerFixes["Rob Kelley"] = "Robert Kelley"
        playerFixes["Mitch Trubisky"] = "Mitchell Trubisky"
        playerFixes["Will Fuller V"] = "Will Fuller"
        playerFixes["Melvin Gordon III"] = "Melvin Gordon"
        playerFixes["Irv Smith Jr."] = "Irv Smith"
        playerFixes["D.K. Metcalf"] = "DK Metcalf"
        teamFixes["cin"] = "Cincinnati Bengals"
        teamFixes["cincinnati"] = "Cincinnati Bengals"
        teamFixes["bengals"] = "Cincinnati Bengals"
        teamFixes["pit"] = "Pittsburgh Steelers"
        teamFixes["pittsburgh"] = "Pittsburgh Steelers"
        teamFixes["steelers"] = "Pittsburgh Steelers"
        teamFixes["cle"] = "Cleveland Browns"
        teamFixes["cle1"] = "Cleveland Browns"
        teamFixes["clv"] = "Cleveland Browns"
        teamFixes["cleveland"] = "Cleveland Browns"
        teamFixes["browns"] = "Cleveland Browns"
        teamFixes["bal"] = "Baltimore Ravens"
        teamFixes["blt"] = "Baltimore Ravens"
        teamFixes["baltimore"] = "Baltimore Ravens"
        teamFixes["ravens"] = "Baltimore Ravens"
        teamFixes["mia"] = "Miami Dolphins"
        teamFixes["miami"] = "Miami Dolphins"
        teamFixes["dolphins"] = "Miami Dolphins"
        teamFixes["nwe"] = "New England Patriots"
        teamFixes["ne"] = "New England Patriots"
        teamFixes["nep"] = "New England Patriots"
        teamFixes["new england"] = "New England Patriots"
        teamFixes["england"] = "New England Patriots"
        teamFixes["patriots"] = "New England Patriots"
        teamFixes["pats"] = "New England Patriots"
        teamFixes["nyj"] = "New York Jets"
        teamFixes["jets"] = "New York Jets"
        teamFixes["ny jets"] = "New York Jets"
        teamFixes["n.y. jets"] = "New York Jets"
        teamFixes["buf"] = "Buffalo Bills"
        teamFixes["buffalo"] = "Buffalo Bills"
        teamFixes["bills"] = "Buffalo Bills"
        teamFixes["ind"] = "Indianapolis Colts"
        teamFixes["indianapolis"] = "Indianapolis Colts"
        teamFixes["colts"] = "Indianapolis Colts"
        teamFixes["jac"] = "Jacksonville Jaguars"
        teamFixes[" jac"] = "Jacksonville Jaguars"
        teamFixes["jac "] = "Jacksonville Jaguars"
        teamFixes["jax"] = "Jacksonville Jaguars"
        teamFixes["jacksonville"] = "Jacksonville Jaguars"
        teamFixes["jaguars"] = "Jacksonville Jaguars"
        teamFixes["hou"] = "Houston Texans"
        teamFixes["houston"] = "Houston Texans"
        teamFixes["hst"] = "Houston Texans"
        teamFixes["texans"] = "Houston Texans"
        teamFixes["ten"] = "Tennessee Titans"
        teamFixes["tennessee"] = "Tennessee Titans"
        teamFixes["titans"] = "Tennessee Titans"
        teamFixes["kc"] = "Kansas City Chiefs"
        teamFixes["kan"] = "Kansas City Chiefs"
        teamFixes["kcc"] = "Kansas City Chiefs"
        teamFixes["kansas"] = "Kansas City Chiefs"
        teamFixes["kansas city"] = "Kansas City Chiefs"
        teamFixes["chiefs"] = "Kansas City Chiefs"
        teamFixes["oak"] = "Las Vegas Raiders"
        teamFixes["oakland"] = "Las Vegas Raiders"
        teamFixes["raiders"] = "Las Vegas Raiders"
        teamFixes["lv"] = "Las Vegas Raiders"
        teamFixes["lvr"] = "Las Vegas Raiders"
        teamFixes["den"] = "Denver Broncos"
        teamFixes["denver"] = "Denver Broncos"
        teamFixes["den3"] = "Denver Broncos"
        teamFixes["broncos"] = "Denver Broncos"
        teamFixes["sd"] = "Los Angeles Chargers"
        teamFixes["lach"] = "Los Angeles Chargers"
        teamFixes["sdc"] = "Los Angeles Chargers"
        teamFixes["san diego"] = "Los Angeles Chargers"
        teamFixes["chargers"] = "Los Angeles Chargers"
        teamFixes["lac"] = "Los Angeles Chargers"
        teamFixes["la chargers"] = "Los Angeles Chargers"
        teamFixes["chi"] = "Chicago Bears"
        teamFixes["chicago"] = "Chicago Bears"
        teamFixes["bears"] = "Chicago Bears"
        teamFixes["min"] = "Minnesota Vikings"
        teamFixes["minnesota"] = "Minnesota Vikings"
        teamFixes["vikings"] = "Minnesota Vikings"
        teamFixes["det"] = "Detroit Lions"
        teamFixes["detroit"] = "Detroit Lions"
        teamFixes["lions"] = "Detroit Lions"
        teamFixes["gb"] = "Green Bay Packers"
        teamFixes["gnb"] = "Green Bay Packers"
        teamFixes["gb "] = "Green Bay Packers"
        teamFixes[" gb"] = "Green Bay Packers"
        teamFixes["gbp"] = "Green Bay Packers"
        teamFixes["green bay"] = "Green Bay Packers"
        teamFixes["packers"] = "Green Bay Packers"
        teamFixes["nyg"] = "New York Giants"
        teamFixes["n.y. giants"] = "New York Giants"
        teamFixes["giants"] = "New York Giants"
        teamFixes["ny giants"] = "New York Giants"
        teamFixes["phi"] = "Philadelphia Eagles"
        teamFixes["philadelphia"] = "Philadelphia Eagles"
        teamFixes["eagles"] = "Philadelphia Eagles"
        teamFixes["dal"] = "Dallas Cowboys"
        teamFixes["dallas"] = "Dallas Cowboys"
        teamFixes["cowboys"] = "Dallas Cowboys"
        teamFixes["was"] = "Washington Redskins"
        teamFixes["wsh"] = "Washington Redskins"
        teamFixes["washington"] = "Washington Redskins"
        teamFixes["wft"] = "Washington Redskins"
        teamFixes["Washington Football Team"] = "Washington Redskins"
        teamFixes["ft"] = "Washington Redskins"
        teamFixes["redskins"] = "Washington Redskins"
        teamFixes["atl"] = "Atlanta Falcons"
        teamFixes["atlanta"] = "Atlanta Falcons"
        teamFixes["atl2"] = "Atlanta Falcons"
        teamFixes["falcons"] = "Atlanta Falcons"
        teamFixes["car"] = "Carolina Panthers"
        teamFixes["carolina"] = "Carolina Panthers"
        teamFixes["panthers"] = "Carolina Panthers"
        teamFixes["no"] = "New Orleans Saints"
        teamFixes["nor"] = "New Orleans Saints"
        teamFixes["nos"] = "New Orleans Saints"
        teamFixes["new orleans"] = "New Orleans Saints"
        teamFixes["saints"] = "New Orleans Saints"
        teamFixes["bucs"] = "Tampa Bay Buccaneers"
        teamFixes["tb"] = "Tampa Bay Buccaneers"
        teamFixes["tbb"] = "Tampa Bay Buccaneers"
        teamFixes["tampa bay"] = "Tampa Bay Buccaneers"
        teamFixes["buccaneers"] = "Tampa Bay Buccaneers"
        teamFixes["tampa"] = "Tampa Bay Buccaneers"
        teamFixes["tam"] = "Tampa Bay Buccaneers"
        teamFixes["sea"] = "Seattle Seahawks"
        teamFixes["seattle"] = "Seattle Seahawks"
        teamFixes["seahawks"] = "Seattle Seahawks"
        teamFixes["sf"] = "San Francisco 49ers"
        teamFixes["sfo"] = "San Francisco 49ers"
        teamFixes["san francisco"] = "San Francisco 49ers"
        teamFixes["ers"] = "San Francisco 49ers"
        teamFixes["49ers"] = "San Francisco 49ers"
        teamFixes["stl"] = "Los Angeles Rams"
        teamFixes["st. louis"] = "Los Angeles Rams"
        teamFixes["st louis"] = "Los Angeles Rams"
        teamFixes["la"] = "Los Angeles Rams"
        teamFixes["ram"] = "Los Angeles Rams"
        teamFixes["larm"] = "Los Angeles Rams"
        teamFixes["rams"] = "Los Angeles Rams"
        teamFixes["sl"] = "Los Angeles Rams"
        teamFixes["la rams"] = "Los Angeles Rams"
        teamFixes["lar"] = "Los Angeles Rams"
        teamFixes["ari"] = "Arizona Cardinals"
        teamFixes["arz"] = "Arizona Cardinals"
        teamFixes["arizona"] = "Arizona Cardinals"
        teamFixes["cardinals"] = "Arizona Cardinals"
    }

    @JvmStatic
    fun normalizePlayerFields(player: Player): Player {
        player.teamName = normalizeTeams(player.teamName)
        player.position = normalizePosition(player.position)
        player.name = normalizeDefenses(normalizeNames(player.name))
        return player
    }

    @JvmStatic
    fun normalizeTeams(team: String): String {
        var low = team.toLowerCase(Locale.ROOT).replace("[^\\x20-\\x7e]".toRegex(), "")
        if (low.split(" ").size > 1
                && (low.split(" ")[1] == "p" || (low.split(" ")[1]
                        == "q"))) {
            low = low.split(" ")[0]
        }
        if (teamFixes.containsKey(low)) {
            return teamFixes[low]!!
        } else if (low.contains("kansas")) {
            return "Kansas City Chiefs"
        } else if (low.contains("diego")) {
            return "San Diego Chargers"
        } else if (low.contains("green") && low.length < 6) {
            return "Green Bay Packers"
        } else if (low.contains("tampa")) {
            return "Tampa Bay Buccaneers"
        } else if (low.contains("orleans") && (low.split(" ".toRegex()).toTypedArray().size == 1 ||
                        low.split(" ".toRegex()).toTypedArray().size > 1 && !low.split(" ".toRegex()).toTypedArray()[0].contains("orleans"))) {
            return "New Orleans Saints"
        } else if (low.contains("la rams")) {
            return "St. Louis Rams"
        } else if (low.contains("francisco")) {
            return "San Francisco 49ers"
        } else if (low.contains("england")) {
            return "New England Patriots"
        } else if (low.contains("nyj")) {
            return "New York Jets"
        } else if (low.contains("tb") && (low.split(" ".toRegex()).toTypedArray().size == 1
                        || low.split(" ".toRegex()).toTypedArray().size == 2 && low.split(" ".toRegex()).toTypedArray()[0].contains("tb"))) {
            return "Tampa Bay Buccaneers"
        } else if (low.contains("mia") && low.length < 5) {
            return "Miami Dolphins"
        } else if (low.contains("nyg")) {
            return "New York Giants"
        } else if (low == "fa" || low == "--" || low == "---" || low == "wr" || low == "") {
            // All random team strings should go here.
            return Constants.NO_TEAM
        }
        return team
    }

    @JvmStatic
    fun normalizeDefenses(inputName: String): String {
        var uName = inputName
        uName = normalizeTeams(uName)
        val name = uName.toLowerCase(Locale.ROOT)
        if (name.contains("cincinnati")) {
            uName = "Bengals D/ST"
        } else if (name.contains("cleveland") && (name.split(" ").size == 1 ||
                        name.split(" ").size == 2 &&
                        name.split(" ")[0].toLowerCase(Locale.ROOT) == "cleveland")) {
            uName = "Browns D/ST"
        } else if (name.contains("pittsburgh")) {
            uName = "Steelers D/ST"
        } else if (name.contains("baltimore")) {
            uName = "Ravens D/ST"
        } else if (name.contains("new england")) {
            uName = "Patriots D/ST"
        } else if (name.contains("miami")) {
            uName = "Dolphins D/ST"
        } else if (name.contains("buffalo")) {
            uName = "Bills D/ST"
        } else if (name.contains("new york jets") || name.contains("ny jets")) {
            uName = "Jets D/ST"
        } else if (name.contains("indianapolis")) {
            uName = "Colts D/ST"
        } else if (name.contains("jacksonville")) {
            uName = "Jaguars D/ST"
        } else if (name.contains("houston")) {
            uName = "Texans D/ST"
        } else if (name.contains("tennessee")) {
            uName = "Titans D/ST"
        } else if (name.contains("san diego") || name.contains("los angeles chargers") ||
                name.contains("la chargers") || name.contains("chargers")) {
            uName = "Chargers D/ST"
        } else if (name.contains("kansas city")) {
            uName = "Chiefs D/ST"
        } else if (name.contains("oakland")) {
            uName = "Raiders D/ST"
        } else if (name.contains("denver")) {
            uName = "Broncos D/ST"
        } else if (name.contains("chicago")) {
            uName = "Bears D/ST"
        } else if (name.contains("minnesota")) {
            uName = "Vikings D/ST"
        } else if (name.contains("detroit")) {
            uName = "Lions D/ST"
        } else if (name.contains("green bay")) {
            uName = "Packers D/ST"
        } else if (name.contains("new york giants")
                || name.contains("ny giants")) {
            uName = "Giants D/ST"
        } else if (name.contains("philadelphia")) {
            uName = "Eagles D/ST"
        } else if (name.contains("dallas") && !(name.split(" ").size > 1 &&
                        "dallas cowboys" != name)) {
            uName = "Cowboys D/ST"
        } else if (name.contains("washington") && !(name.split(" ").size > 1 &&
                        "washington redskins" != name)) {
            uName = "Redskins D/ST"
        } else if (name.contains("new orleans")) {
            uName = "Saints D/ST"
        } else if (name.contains("atlanta")) {
            uName = "Falcons D/ST"
        } else if (name.contains("carolina")) {
            uName = "Panthers D/ST"
        } else if (name.contains("tamba bay") || name.contains("tampa bay")) {
            uName = "Buccaneers D/ST"
        } else if (name.contains("san fran") || name.contains("san francisco")) {
            uName = "49ers D/ST"
        } else if (name.contains("st. louis") || name.contains("st louis") ||
                name.contains("la rams") || name.contains("los angeles rams")) {
            uName = "Rams D/ST"
        } else if (name.contains("arizona")) {
            uName = "Cardinals D/ST"
        } else if (name.contains("seattle")) {
            uName = "Seahawks D/ST"
        }
        return uName
    }

    @JvmStatic
    fun normalizeNames(inputName: String): String {
        var playerName = inputName
        if (playerFixes.containsKey(playerName)) {
            playerName = playerFixes[playerName]!!

        } else if (playerName.contains(Constants.DST)) {
            playerName = normalizeDefenses(playerName)
        } else if (playerName.contains("Veon") && playerName.contains("Bell")) {
            playerName = "Le'Veon Bell"
        } else if (playerName.endsWith("II")) {
            playerName = playerName.replace(" II", "")
        } else if (playerName.endsWith("III")) {
            playerName = playerName.replace(" III", "")
        }
        return playerName
    }

    private fun normalizePosition(inputPosition: String): String {
        var position = inputPosition
        when (position) {
            "PK" -> position = Constants.K
            "DEF", "DST" -> position = Constants.DST
            "WR,RB", "WR/RB", "RB,WR", "CB,RB", "RB/WR" -> position = Constants.RB
            "CB,DB,WR" -> position = Constants.WR
            "TE,RB" -> position = Constants.TE
        }
        return position
    }

    @JvmStatic
    fun conditionallyAddContext(oldPlayer: Player, inputPlayer: Player): Player {
        var newPlayer = inputPlayer
        oldPlayer.handleNewValue(newPlayer.auctionValue)
        newPlayer = normalizePlayerFields(newPlayer)
        if (StringUtils.isBlank(oldPlayer.teamName) && !StringUtils.isBlank(newPlayer.teamName)) {
            oldPlayer.teamName = newPlayer.teamName
        }
        if (StringUtils.isBlank(oldPlayer.position) && !StringUtils.isBlank(newPlayer.position)) {
            oldPlayer.position = newPlayer.position
        }
        if ((oldPlayer.age == null || oldPlayer.age!! < 18) &&
                newPlayer.age != null && newPlayer.age!! > 18) {
            oldPlayer.age = newPlayer.age
        }
        if (oldPlayer.experience < 0) {
            oldPlayer.experience = newPlayer.experience
        }
        return oldPlayer
    }

    @JvmStatic
    fun getPlayerFromRankings(name: String, team: String, pos: String, newValue: Double): Player {
        var player = Player()
        player.name = name
        player.position = pos
        player.teamName = team
        player = normalizePlayerFields(player)
        player.handleNewValue(newValue)
        return player
    }
}