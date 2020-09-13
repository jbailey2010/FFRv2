package com.devingotaswitch.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.devingotaswitch.rankings.domain.*
import com.devingotaswitch.rankings.domain.RosterSettings.Flex
import com.devingotaswitch.rankings.domain.projections.PlayerProjection
import java.util.*

object DBUtils {
    @JvmStatic
    fun getSelectAllString(tableName: String): String {
        return "SELECT * FROM " +
                tableName
    }

    @JvmStatic
    fun getSelectCountString(tableName: String): String {
        return "SELECT count(*) FROM $tableName"
    }

    @JvmStatic
    fun getSelectSingleString(tableName: String, idColumn: String, idValue: String): String {
        return "SELECT * FROM " +
                tableName +
                " WHERE " +
                idColumn +
                " = '" +
                idValue +
                "'"
    }

    @JvmStatic
    fun getSelectThreeAttrString(tableName: String, idOne: String, idTwo: String, idThree: String,
                                 valueOne: String, valueTwo: String, valueThree: String): String {
        return "SELECT * FROM " +
                tableName +
                " WHERE " +
                idOne +
                " = '" +
                valueOne +
                "' AND " +
                idTwo +
                " = '" +
                valueTwo +
                "' AND " +
                idThree +
                " = '" +
                valueThree +
                "'"
    }

    @JvmStatic
    fun getDeleteAllString(tableName: String): String {
        return "DELETE FROM " +
                tableName
    }

    @JvmStatic
    fun getUpdateAndDeleteKeyString(idColumn: String): String {
        return idColumn +
                " = ?"
    }

    @JvmStatic
    fun getMultiKeyUpdateAndDeleteKeyString(columnOne: String, columnTwo: String): String {
        return columnOne +
                " = ? AND " +
                columnTwo +
                " = ?"
    }

    @JvmStatic
    fun updatedValuesToContentValues(updatedValues: Map<String?, String?>): ContentValues {
        val values = ContentValues()
        for (key in updatedValues.keys) {
            values.put(key, updatedValues[key])
        }
        return values
    }

    @JvmStatic
    fun leagueToContentValues(league: LeagueSettings): ContentValues {
        val values = ContentValues()
        values.put(Constants.NAME_COLUMN, sanitizeName(league.name))
        values.put(Constants.TEAM_COUNT_COLUMN, league.teamCount)
        values.put(Constants.IS_SNAKE_COLUMN, league.isSnake)
        values.put(Constants.IS_AUCTION_COLUMN, league.isAuction)
        values.put(Constants.IS_DYNASTY_STARTUP_COLUMN, league.isDynasty)
        values.put(Constants.IS_DYNASTY_ROOKIE_COLUMN, league.isRookie)
        values.put(Constants.IS_BEST_BALL_COLUMN, league.isBestBall)
        values.put(Constants.AUCTION_BUDGET_COLUMN, league.auctionBudget)
        values.put(Constants.CURRENT_LEAGUE_COLUMN, league.isCurrentLeague)
        values.put(Constants.SCORING_ID_COLUMN, league.scoringSettings.id)
        values.put(Constants.ROSTER_ID_COLUMN, league.rosterSettings.id)
        return values
    }

    @JvmStatic
    fun cursorToLeague(result: Cursor, roster: RosterSettings, scoring: ScoringSettings): LeagueSettings {
        return LeagueSettings(
                desanitizeName(result.getString(result.getColumnIndex(Constants.NAME_COLUMN))),
                result.getInt(result.getColumnIndex(Constants.TEAM_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.IS_SNAKE_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.IS_AUCTION_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.IS_DYNASTY_STARTUP_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.IS_DYNASTY_ROOKIE_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.IS_BEST_BALL_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.CURRENT_LEAGUE_COLUMN)) != 0,
                result.getInt(result.getColumnIndex(Constants.AUCTION_BUDGET_COLUMN)),
                scoring,
                roster
        )
    }

    @JvmStatic
    fun scoringToContentValues(scoring: ScoringSettings): ContentValues {
        val values = ContentValues()
        values.put(Constants.SCORING_ID_COLUMN, scoring.id)
        values.put(Constants.PASSING_TDS_COLUMN, scoring.passingTds)
        values.put(Constants.RUSHING_TDS_COLUMN, scoring.rushingTds)
        values.put(Constants.RECEIVING_TDS_COLUMN, scoring.receivingTds)
        values.put(Constants.FUMBLES_COLUMN, scoring.fumbles)
        values.put(Constants.INTERCEPTIONS_COLUMN, scoring.interceptions)
        values.put(Constants.PASSING_YARDS_COLUMN, scoring.passingYards)
        values.put(Constants.RUSHING_YARDS_COLUMN, scoring.rushingYards)
        values.put(Constants.RECEIVING_YARDS_COLUMN, scoring.receivingYards)
        values.put(Constants.RECEPTIONS_COLUMN, scoring.receptions)
        return values
    }

    @JvmStatic
    fun cursorToScoring(result: Cursor): ScoringSettings {
        return ScoringSettings(
                result.getString(result.getColumnIndex(Constants.SCORING_ID_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.PASSING_TDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RUSHING_TDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RECEIVING_TDS_COLUMN)),
                result.getDouble(result.getColumnIndex(Constants.FUMBLES_COLUMN)),
                result.getDouble(result.getColumnIndex(Constants.INTERCEPTIONS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.PASSING_YARDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RUSHING_YARDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RECEIVING_YARDS_COLUMN)),
                result.getDouble(result.getColumnIndex(Constants.RECEPTIONS_COLUMN))
        )
    }

    @JvmStatic
    fun rosterToContentValues(roster: RosterSettings): ContentValues {
        val values = ContentValues()
        values.put(Constants.ROSTER_ID_COLUMN, roster.id)
        values.put(Constants.QB_COUNT_COLUMN, roster.qbCount)
        values.put(Constants.RB_COUNT_COLUMN, roster.rbCount)
        values.put(Constants.WR_COUNT_COLUMN, roster.wrCount)
        values.put(Constants.TE_COUNT_COLUMN, roster.teCount)
        values.put(Constants.DST_COUNT_COLUMN, roster.dstCount)
        values.put(Constants.K_COUNT_COLUMN, roster.kCount)
        values.put(Constants.BENCH_COUNT_COLUMN, roster.benchCount)
        values.put(Constants.RBWR_COUNT_COLUMN, roster.flex!!.rbwrCount)
        values.put(Constants.RBTE_COUNT_COLUMN, roster.flex!!.rbteCount)
        values.put(Constants.RBWRTE_COUNT_COLUMN, roster.flex!!.rbwrteCount)
        values.put(Constants.WRTE_COUNT_COLUMN, roster.flex!!.wrteCount)
        values.put(Constants.QBRBWRTE_COUNT_COLUMN, roster.flex!!.qbrbwrteCount)
        return values
    }

    @JvmStatic
    fun cursorToRoster(result: Cursor): RosterSettings {
        val flex = Flex()
        flex.rbwrCount = result.getInt(result.getColumnIndex(Constants.RBWR_COUNT_COLUMN))
        flex.rbteCount = result.getInt(result.getColumnIndex(Constants.RBTE_COUNT_COLUMN))
        flex.rbwrteCount = result.getInt(result.getColumnIndex(Constants.RBWRTE_COUNT_COLUMN))
        flex.wrteCount = result.getInt(result.getColumnIndex(Constants.WRTE_COUNT_COLUMN))
        flex.qbrbwrteCount = result.getInt(result.getColumnIndex(Constants.QBRBWRTE_COUNT_COLUMN))
        return RosterSettings(
                result.getString(result.getColumnIndex(Constants.ROSTER_ID_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.QB_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RB_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.WR_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.TE_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.DST_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.K_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.BENCH_COUNT_COLUMN)),
                flex
        )
    }

    @JvmStatic
    fun teamToContentValues(team: Team): ContentValues {
        val values = ContentValues()
        values.put(Constants.TEAM_NAME_COLUMN, team.name)
        values.put(Constants.OLINE_RANKS_COLUMN, team.oLineRanks)
        values.put(Constants.DRAFT_CLASS_COLUMN, team.draftClass)
        values.put(Constants.QB_SOS_COLUMN, team.qbSos)
        values.put(Constants.RB_SOS_COLUMN, team.rbSos)
        values.put(Constants.WR_SOS_COLUMN, team.wrSos)
        values.put(Constants.TE_SOS_COLUMN, team.teSos)
        values.put(Constants.DST_SOS_COLUMN, team.dstSos)
        values.put(Constants.K_SOS_COLUMN, team.kSos)
        values.put(Constants.BYE_COLUMN, team.bye)
        values.put(Constants.FREE_AGENCY_COLUMN, team.faClass)
        values.put(Constants.SCHEDULE_COLUMN, team.schedule)
        return values
    }

    @JvmStatic
    fun cursorToTeam(result: Cursor): Team {
        val team = Team()
        team.name = result.getString(result.getColumnIndex(Constants.TEAM_NAME_COLUMN))
        team.oLineRanks = result.getString(result.getColumnIndex(Constants.OLINE_RANKS_COLUMN))
        team.draftClass = result.getString(result.getColumnIndex(Constants.DRAFT_CLASS_COLUMN))
        team.qbSos = result.getDouble(result.getColumnIndex(Constants.QB_SOS_COLUMN))
        team.rbSos = result.getDouble(result.getColumnIndex(Constants.RB_SOS_COLUMN))
        team.wrSos = result.getDouble(result.getColumnIndex(Constants.WR_SOS_COLUMN))
        team.teSos = result.getDouble(result.getColumnIndex(Constants.TE_SOS_COLUMN))
        team.dstSos = result.getDouble(result.getColumnIndex(Constants.DST_SOS_COLUMN))
        team.kSos = result.getDouble(result.getColumnIndex(Constants.K_SOS_COLUMN))
        team.bye = result.getString(result.getColumnIndex(Constants.BYE_COLUMN))
        team.faClass = result.getString(result.getColumnIndex(Constants.FREE_AGENCY_COLUMN))
        team.schedule = result.getString(result.getColumnIndex(Constants.SCHEDULE_COLUMN))
        return team
    }

    @JvmStatic
    fun cursorToPlayer(result: Cursor): Player {
        val player = cursorToPlayerBasic(result)
        player.adp = result.getDouble(result.getColumnIndex(Constants.PLAYER_ADP_COLUMN))
        player.ecr = result.getDouble(result.getColumnIndex(Constants.PLAYER_ECR_COLUMN))
        player.dynastyRank = result.getDouble(result.getColumnIndex(Constants.PLAYER_DYNASTY_COLUMN))
        player.rookieRank = result.getDouble(result.getColumnIndex(Constants.PLAYER_ROOKIE_COLUMN))
        player.bestBallRank = result.getDouble(result.getColumnIndex(Constants.PLAYER_BEST_BALL_COLUMN))
        player.risk = result.getDouble(result.getColumnIndex(Constants.PLAYER_RISK_COLUMN))
        player.age = result.getInt(result.getColumnIndex(Constants.PLAYER_AGE_COLUMN))
        player.experience = result.getInt(result.getColumnIndex(Constants.PLAYER_EXPERIENCE_COLUMN))
        player.stats = desanitizeStats(result.getString(result.getColumnIndex(Constants.PLAYER_STATS_COLUMN)))
        player.injuryStatus = result.getString(result.getColumnIndex(Constants.PLAYER_INJURED_COLUMN))
        player.auctionValue = result.getDouble(result.getColumnIndex(Constants.AUCTION_VALUE_COLUMN))
        player.playerProjection = PlayerProjection(result.getString(result.getColumnIndex(Constants.PLAYER_PROJECTION_COLUMN)))
        player.paa = result.getDouble(result.getColumnIndex(Constants.PLAYER_PAA_COLUMN))
        player.xval = (result.getDouble(result.getColumnIndex(Constants.PLAYER_XVAL_COLUMN)))
        player.vols = result.getDouble(result.getColumnIndex(Constants.PLAYER_VORP_COLUMN))
        return player
    }

    @JvmStatic
    fun cursorToPlayerBasic(result: Cursor): Player {
        val player = Player()
        player.name = desanitizeName(result.getString(result.getColumnIndex(Constants.PLAYER_NAME_COLUMN)))
        player.position = result.getString(result.getColumnIndex(Constants.PLAYER_POSITION_COLUMN))
        player.teamName = result.getString(result.getColumnIndex(Constants.TEAM_NAME_COLUMN))
        return player
    }

    @JvmStatic
    fun playerToContentValues(player: Player): ContentValues {
        val values = ContentValues()
        values.put(Constants.PLAYER_NAME_COLUMN, sanitizeName(player.name))
        values.put(Constants.PLAYER_POSITION_COLUMN, player.position)
        values.put(Constants.TEAM_NAME_COLUMN, player.teamName)
        values.put(Constants.PLAYER_AGE_COLUMN, player.age)
        values.put(Constants.PLAYER_EXPERIENCE_COLUMN, player.experience)
        values.put(Constants.PLAYER_ECR_COLUMN, player.ecr)
        values.put(Constants.PLAYER_ADP_COLUMN, player.adp)
        values.put(Constants.PLAYER_DYNASTY_COLUMN, player.dynastyRank)
        values.put(Constants.PLAYER_ROOKIE_COLUMN, player.rookieRank)
        values.put(Constants.PLAYER_BEST_BALL_COLUMN, player.bestBallRank)
        values.put(Constants.PLAYER_RISK_COLUMN, player.risk)
        values.put(Constants.PLAYER_STATS_COLUMN, sanitizeStats(player.stats))
        values.put(Constants.PLAYER_INJURED_COLUMN, player.injuryStatus)
        values.put(Constants.AUCTION_VALUE_COLUMN, player.auctionValue)
        values.put(Constants.PLAYER_PROJECTION_COLUMN, player.playerProjection.toString())
        values.put(Constants.PLAYER_PAA_COLUMN, player.paa)
        values.put(Constants.PLAYER_XVAL_COLUMN, player.xval)
        values.put(Constants.PLAYER_VORP_COLUMN, player.vols)
        return values
    }

    @JvmStatic
    fun playerProjectionToContentValues(player: Player): ContentValues {
        val date = Constants.DATE_FORMAT.format(Calendar.getInstance().time)
        val values = ContentValues()
        values.put(Constants.PLAYER_NAME_COLUMN, sanitizeName(player.name))
        values.put(Constants.PLAYER_POSITION_COLUMN, player.position)
        values.put(Constants.TEAM_NAME_COLUMN, player.teamName)
        values.put(Constants.PLAYER_PROJECTION_DATE_COLUMN, date)
        values.put(Constants.PLAYER_PROJECTION_COLUMN, player.playerProjection.toString())
        return values
    }

    @JvmStatic
    fun cursorToPlayerProjection(result: Cursor): DailyProjection {
        val projection = DailyProjection()
        val player = Player()
        player.name = desanitizeName(result.getString(result.getColumnIndex(Constants.PLAYER_NAME_COLUMN)))
        player.position = result.getString(result.getColumnIndex(Constants.PLAYER_POSITION_COLUMN))
        player.teamName = result.getString(result.getColumnIndex(Constants.TEAM_NAME_COLUMN))
        projection.playerKey = player.uniqueId
        projection.date = result.getString(result.getColumnIndex(Constants.PLAYER_PROJECTION_DATE_COLUMN))
        projection.playerProjection = PlayerProjection(result.getString(
                result.getColumnIndex(Constants.PLAYER_PROJECTION_COLUMN)))
        return projection
    }

    private fun sanitizeStats(input: String?): String? {
        return input?.replace("%", "PER")?.replace(": ", "SPL")
                ?.replace("\n".toRegex(), "NL")
                ?: input
    }

    private fun desanitizeStats(input: String?): String? {
        return input?.replace("PER", "%")?.replace("SPL", ": ")
                ?.replace("NL", "\n")
                ?: input
    }

    @JvmStatic
    fun sanitizeName(name: String?): String {
        return name!!.replace("'", "APOS")
    }

    private fun desanitizeName(sanitizedName: String): String {
        return sanitizedName.replace("APOS", "'")
    }
}