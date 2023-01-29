/*
 * Copyright 2016 Stefan 'Namikon' Thomanek <sthomanek at gmail dot com> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.usrv.amdiforge.config;

import java.io.File;

import eu.usrv.yamcore.config.ConfigManager;

public class AMDIConfig extends ConfigManager {

    public AMDIConfig(File pConfigBaseDirectory, String pModCollectionDirectory, String pModID) {
        super(pConfigBaseDirectory, pModCollectionDirectory, pModID);
    }

    public boolean UseMySQL;
    public String MySQL_Server;
    public String MySQL_DB;
    public String MySQL_User;
    public String MySQL_Password;
    public boolean TraceAndExportUnknownEntities;
    public int SpawnEventReportLevel;
    public boolean EnableGraveHelpCommand;
    public boolean EnableGraveAdminCommand;

    @Override
    protected void PreInit() {
        UseMySQL = true;
        MySQL_Server = "";
        MySQL_DB = "";
        MySQL_User = "";
        MySQL_Password = "";
        TraceAndExportUnknownEntities = false;
        SpawnEventReportLevel = 0;
    }

    @Override
    protected void Init() {
        UseMySQL = _mainConfig.getBoolean(
                "UseMySQL",
                "MySQL",
                UseMySQL,
                "Set false to skip using MySQL. Set this on true on server.");
        MySQL_Server = _mainConfig.getString(
                "MySQL_Server",
                "MySQL",
                MySQL_Server,
                "Your MySQL Server. !!! SET THIS ONLY SERVERSIDE !!!");
        MySQL_DB = _mainConfig.getString(
                "MySQL_DB",
                "MySQL",
                MySQL_Server,
                "Your MySQL Database Name. !!! SET THIS ONLY SERVERSIDE !!!");
        MySQL_User = _mainConfig.getString(
                "MySQL_User",
                "MySQL",
                MySQL_Server,
                "Your MySQL Username. !!! SET THIS ONLY SERVERSIDE !!!");
        MySQL_Password = _mainConfig.getString(
                "MySQL_Password",
                "MySQL",
                MySQL_Server,
                "Your MySQL Password. !!! SET THIS ONLY SERVERSIDE !!!");
        TraceAndExportUnknownEntities = _mainConfig.getBoolean(
                "TraceAndExportUnknownEntities",
                "SpawnLimiter",
                TraceAndExportUnknownEntities,
                "If set to true, yet unknown entities that spawn will be added to the config file with an empty world-set");
        SpawnEventReportLevel = _mainConfig.getInt(
                "SpawnEventReportLevel",
                "SpawnLimiter",
                0,
                2,
                SpawnEventReportLevel,
                "0: Disabled 1: Report Blocked only 2: Report Blocked and allowed");
        EnableGraveHelpCommand = _mainConfig.getBoolean(
                "EnableGraveHelpCommand",
                "Modules",
                false,
                "Set to true to enable the /gravehelp request command to provide users with a lookup code");
        EnableGraveAdminCommand = _mainConfig.getBoolean(
                "EnableGraveAdminCommand",
                "Modules",
                false,
                "Set to true to enable the /graveadmin command for Operators, to work with GraveFiles");
    }

    @Override
    protected void PostInit() {

    }
}
