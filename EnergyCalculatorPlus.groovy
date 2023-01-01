/**
 *
 * Energy Calculator Plus
 *
 * Copyright 2022 Ryan Elliott
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * v1.0		RLE		Creation
 * v1.1		RLE		Substantial updates to the UI along with functionality. 
 */
 
definition(
    name: "Energy Calculator Plus",
    namespace: "rle.sg+",
    author: "Ryan Elliott",
    description: "Creates a table to track the cost of energy meters.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
	page(name: "mainPage")
	page(name: "pageSelectVariables")
}

def mainPage() {
    
	if(state.energies == null) state.energies = [:]
	if(state.energiesList == null) state.energiesList = []
	dynamicPage(name: "mainPage", uninstall: true, install: true) {
		section("<b>App Name</b>",hideable: true, hidden: true) {
            label title: "<b>***Enter a name for this app.***</b>", required:true, width: 4
        }

		section("<b>Device Selection</b>",hideable: true, hidden: true) {
			input "energies", "capability.energyMeter", title: "Select Energy Devices to Measure Cost", multiple: true, submitOnChange: true, width: 4
		}

        section("<b>Energy Rate Information</b>",hideable: true, hidden: true) {
			input "energyRate", "string", title: "What is your energy rate per kWh?", required: false, default: 1, width: 4, submitOnChange: true
			if(!energyRate) {state.energyRate = 1} else {state.energyRate = energyRate}
            input "symbol", "string", title: "What is your currency symbol? (Optional)", required: false, width: 4, submitOnChange: true
		}

		section{
			href(name: "hrefSelectVariables", title: "Click here to add and/or remove variable links",
			description: "", page: "pageSelectVariables", width:4)
		}

		section{
			energies.each {dev ->
				if(!state.energies["$dev.id"]) {
					state.energies["$dev.id"] = [todayEnergy: 0, dayStart: dev.currentEnergy, var: "",thisWeekEnergy: 0,thisMonthEnergy: 0,lastWeekEnergy: 0,lastMonthEnergy: 0]
                    state.energiesList += dev.id
				}
			}
			if(energies) {
				if(energies.id.sort() != state.energiesList.sort()) { //something was removed
					state.energiesList = energies.id
					Map newState = [:]
					energies.each{d ->  newState["$d.id"] = state.energies["$d.id"]}
					state.energies = newState
				}
				updated()
				paragraph displayTable()
				input "refresh", "button", title: "Refresh Table", width: 2
				paragraph ""
                input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: false, displayDuringSetup: false, required: false, width: 2
				input "traceOutput", "bool", title: "Enable trace logging?", defaultValue: false, displayDuringSetup: false, required: false, width: 2
			}
		}
	}
}

def pageSelectVariables() {
	logTrace "Loading variable table..."
	dynamicPage(name: "pageSelectVariables", uninstall: false, install: false, nextPage: "mainPage") {
		section{
			if(energies)
					paragraph displayVariableTable()
			if(state.newTodayVar) {
				logTrace "newTodayVar is ${state.newTodayVar}"
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.energies[state.newTodayVar].todayVar = newVar
					state.remove("newTodayVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remTodayVar) {
				removeInUseGlobalVar(state.energies[state.remTodayVar].todayVar)
				state.energies[state.remTodayVar].todayVar = ""
				state.remove("remTodayVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			} else if(state.newWeekVar) {
				logTrace "newWeekVar is ${state.newWeekVar}"
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.energies[state.newWeekVar].weekVar = newVar
					state.remove("newWeekVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remWeekVar) {
				removeInUseGlobalVar(state.energies[state.remWeekVar].weekVar)
				state.energies[state.remWeekVar].weekVar = ""
				state.remove("remWeekVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			} else if(state.newMonthVar) {
				logTrace "newMonthVar is ${state.newMonthVar}"
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.energies[state.newMonthVar].monthVar = newVar
					state.remove("newMonthVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remMonthVar) {
				removeInUseGlobalVar(state.energies[state.remMonthVar].monthVar)
				state.energies[state.remMonthVar].monthVar = ""
				state.remove("remMonthVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			} else if(state.newTodayTotalVar) {
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.todayTotalVar = newVar
					state.remove("newTodayTotalVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remTodayTotalVar) {
				removeInUseGlobalVar(state.todayTotalVar)
				state.todayTotalVar = ""
				state.remove("remTodayTotalVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			} else if(state.newWeekTotalVar) {
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.weekTotalVar = newVar
					state.remove("newWeekTotalVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remWeekTotalVar) {
				removeInUseGlobalVar(state.weekTotalVar)
				state.weekTotalVar = ""
				state.remove("remWeekTotalVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			} else if(state.newMonthTotalVar) {
				List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
				input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
				if(newVar) {
					addInUseGlobalVar(newVar)
					state.monthTotalVar = newVar
					state.remove("newMonthTotalVar")
					app.removeSetting("newVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
			} else if(state.remMonthTotalVar) {
				removeInUseGlobalVar(state.monthTotalVar)
				state.monthTotalVar = ""
				state.remove("remMonthTotalVar")
				paragraph "<script>{changeSubmit(this)}</script>"
			}
		}
	}
}

String displayTable() {
	logDebug "Table display called"
	String str = "<script src='https://code.iconify.design/iconify-icon/1.0.0/iconify-icon.min.js'></script>"
	str += "<style>.mdl-data-table tbody tr:hover{background-color:inherit} .tstat-col td,.tstat-col th { padding:8px 8px;text-align:center;font-size:12px} .tstat-col td {font-size:15px }" +
		"</style><div style='overflow-x:auto'><table class='mdl-data-table tstat-col' style=';border:2px solid black'>" +
		"<thead><tr style='border-bottom:2px solid black'><th style='border-right:2px solid black'>Meter</th>" +
		"<th>Energy Use Today</th>" +
		"<th>Today's Cost</th>" +
		"<th>Energy Use This Week</th>" +
		"<th>Energy Cost This Week</th>" +
		"<th>Energy Use Last Week</th>" +
		"<th>Energy Use This Month</th>" +
		"<th>Energy Cost This Month</th>" +
		"<th>Energy Use Last Month</th></tr></thead>"
	updateDeviceEnergy()
	energies.sort{it.displayName.toLowerCase()}.each {dev ->
		device = state.energies["$dev.id"]
		start = device.dayStart
		todayEnergy = device.todayEnergy
        todayCost = device.todayCost
		thisWeekEnergy = device.thisWeekEnergy
		thisWeekCost = device.thisWeekCost
		thisMonthEnergy = device.thisMonthEnergy
		thisMonthCost = device.thisMonthCost
		lastWeekEnergy = device.lastWeekEnergy
		lastMonthEnergy = device.lastMonthEnergy
		String devLink = "<a href='/device/edit/$dev.id' target='_blank' title='Open Device Page for $dev'>$dev"
		str += "<tr style='color:black'><td style='border-right:2px solid black'>$devLink</td>" +
			"<td style='color:#4c2c92'><b>$todayEnergy</b></td>" +
			"<td title='Money spent running ${dev}' style='color:#4c2c92'><b>$todayCost</b></td>" +
			"<td style='color:#007cbe'><b>$thisWeekEnergy</b></td>" +
			"<td title='Money spent running ${dev}' style='color:#007cbe'><b>$thisWeekCost</b></td>" +
			"<td style='color:#007cbe'><b>$lastWeekEnergy</b></td>" +
			"<td style='color:#981b1e'><b>$thisMonthEnergy</b></td>" +
			"<td title='Money spent running $dev' style='color:#981b1e'><b>$thisMonthCost</b></td>" +
			"<td style='color:#981b1e'><b>$lastMonthEnergy</b></td></tr>"
	}
	todayTotalEnergy = state.todayTotalEnergy
	totalCostToday = state.totalCostToday
	thisWeekTotal = state.thisWeekTotal
	totalCostWeek = state.totalCostWeek
	thisMonthTotal = state.thisMonthTotal
	totalCostMonth = state.totalCostMonth
	lastWeekTotal = state.lastWeekTotal ?: 0
	lastMonthTotal = state.lastMonthTotal ?: 0
    str += "<tr style='color:black'><td style='border-right:2px solid black'>Total</td>" +
			"<td style='color:#333366'><b>$todayTotalEnergy</b></td>" +
			"<td title='Money spent running $dev' style='color:#333366'><b>$totalCostToday</b></td>" +
			"<td style='color:#008a1e'><b>$thisWeekTotal</b></td>" +
			"<td title='Money spent running $dev' style='color:#008a1e'><b>$totalCostWeek</b></td>" +
			"<td style='color:#008a1e'><b>$lastWeekTotal</b></td>" +
			"<td style='color:#046b99'><b>$thisMonthTotal</b></td>" +
			"<td title='Money spent running $dev' style='color:#046b99'><b>$totalCostMonth</b></td>" +
			"<td style='color:#046b99'><b>$lastMonthTotal</b></td></tr>"
	str += "</table></div>"
	str
}

String displayVariableTable() {
	logDebug "Variable table display called"
	String str = "<script src='https://code.iconify.design/iconify-icon/1.0.0/iconify-icon.min.js'></script>"
	str += "<style>.mdl-data-table tbody tr:hover{background-color:inherit} .tstat-col td,.tstat-col th { padding:8px 8px;text-align:center;font-size:12px} .tstat-col td {font-size:15px }" +
		"</style><div style='overflow-x:auto'><table class='mdl-data-table tstat-col' style=';border:2px solid black'>" +
		"<thead><tr style='border-bottom:2px solid black'><th style='border-right:2px solid black'>Meter</th>" +
		"<th>Today's Cost Variable</th>" +
		"<th>This Week Cost Variable</th>" +
		"<th>This Month Cost Variable</th></tr></thead>"
	energies.sort{it.displayName.toLowerCase()}.each {dev ->
		device = state.energies["$dev.id"]
		String todayVar = device.todayVar
		String weekVar = device.weekVar
		String monthVar = device.monthVar
		String devLink = "<a href='/device/edit/$dev.id' target='_blank' title='Open Device Page for $dev'>$dev"
		String todaysVar = todayVar ? buttonLink("noToday$dev.id", todayVar, "purple") : buttonLink("today$dev.id", "Select", "red")
		String weeksVar = weekVar ? buttonLink("noWeek$dev.id", weekVar, "purple") : buttonLink("week$dev.id", "Select", "red")
		String monthsVar = monthVar ? buttonLink("noMonth$dev.id", monthVar, "purple") : buttonLink("month$dev.id", "Select", "red")
		str += "<tr style='color:black'><td style='border-right:2px solid black'>$devLink</td>" +
		"<td title='${todayVar ? "Deselect $todayVar" : "Set a string hub variable to todays cost value"}'>$todaysVar</td>" +
		"<td title='${weekVar ? "Deselect $weekVar" : "Set a string hub variable to this weeks cost value"}'>$weeksVar</td>" +
		"<td title='${monthVar ? "Deselect $monthVar" : "Set a string hub variable to this months cost value"}'>$monthsVar</td></tr>"
	}
	String todayTotalVar = state.todayTotalVar
	String weekTotalVar = state.weekTotalVar
	String monthTotalVar = state.monthTotalVar
	String todaysTotalVar = todayTotalVar ? buttonLink("noVarTodayTotal", todayTotalVar, "purple") : buttonLink("varTodayTotal", "Select", "red")
	String weeksTotalVar = weekTotalVar ? buttonLink("noVarWeekTotal", weekTotalVar, "purple") : buttonLink("varWeekTotal", "Select", "red")
	String monthsTotalVar = monthTotalVar ? buttonLink("noVarMonthTotal", monthTotalVar, "purple") : buttonLink("varMonthTotal", "Select", "red")
	str += "<tr style='color:black'><td style='border-right:2px solid black'>Totals</td>" +
		"<td title='${todayVar ? "Deselect $todayVar" : "Set a string hub variable to todays cost value"}'>$todaysTotalVar</td>" +
		"<td title='${weekVar ? "Deselect $weekVar" : "Set a string hub variable to this weeks cost value"}'>$weeksTotalVar</td>" +
		"<td title='${monthVar ? "Deselect $monthVar" : "Set a string hub variable to this months cost value"}'>$monthsTotalVar</td></tr>"
	str += "</table></div>"
	str
}

String buttonLink(String btnName, String linkText, color = "#1A77C9", font = "15px") {
	"<div class='form-group'><input type='hidden' name='${btnName}.type' value='button'></div><div><div class='submitOnChange' onclick='buttonClick(this)' style='color:$color;cursor:pointer;font-size:$font'>$linkText</div></div><input type='hidden' name='settings[$btnName]' value=''>"
}

void appButtonHandler(btn) {
	logDebug "btn is ${btn}"
	if(btn == "varTodayTotal") state.newTodayTotalVar = btn
	else if(btn == "noVarTodayTotal") state.remTodayTotalVar = btn
    else if(btn == "varWeekTotal") state.newWeekTotalVar = btn
	else if(btn == "noVarWeekTotal") state.remWeekTotalVar = btn
    else if(btn == "varMonthTotal") state.newMonthTotalVar = btn
	else if(btn == "noVarMonthTotal") state.remMonthTotalVar = btn
	else if(btn.startsWith("today")) state.newTodayVar = btn.minus("today")
	else if(btn.startsWith("noToday")) state.remTodayVar = btn.minus("noToday")
    else if(btn.startsWith("week")) state.newWeekVar = btn.minus("week")
	else if(btn.startsWith("noWeek")) state.remWeekVar = btn.minus("noWeek")
    else if(btn.startsWith("month")) state.newMonthVar = btn.minus("month")
	else if(btn.startsWith("noMonth")) state.remMonthVar = btn.minus("noMonth")
}

void updated() {
	logTrace "Updated app"
	unsubscribe()
	unschedule()
	initialize()
}

void installed() {
	log.warn "Installed app"
	initialize()
}

void uninstalled() {
	log.warn "Uninstalling app"
	removeAllInUseGlobalVar()
}

void initialize() {
	logTrace "Initialized app"
	schedule("0 1 0 * * ?",dayStartEnergy)
	schedule("40 0 0 ? * SUN *",resetWeekly)
	schedule("45 0 0 1 * ? *",resetMonthly)
	subscribe(energies, "energy", energyHandler)
}

void energyHandler(evt) {
    logDebug "Energy change for ${evt.device}"
	updateDeviceEnergy()
}

void updateDeviceEnergy() {
	logDebug "Start energy update"
	def todayTotalEnergy = 0
	def thisWeekTotal = 0
	def thisMonthTotal = 0
	energies.each {dev ->
		device = state.energies["$dev.id"]
		String thisVar = device.var
		BigDecimal start = device.dayStart ?: 0
		BigDecimal thisWeek = device.thisWeekEnergy ?: 0
		BigDecimal thisWeekStart = device.weekStart ?: 0
		BigDecimal thisMonth = device.thisMonthEnergy ?: 0
		BigDecimal thisMonthStart = device.monthStart ?: 0
		if(dev.currentEnergy) {nowEnergy = dev.currentEnergy} else {nowEnergy = 0}
		todayEnergy = nowEnergy - start
		if(todayEnergy < 0) {todayEnergy = 0}
		logTrace "${dev} energy today is ${todayEnergy}"
		device.todayEnergy = todayEnergy
		todayTotalEnergy = todayTotalEnergy + todayEnergy
		thisWeek = thisWeekStart + todayEnergy
		thisWeekTotal = thisWeekTotal + thisWeek
		device.thisWeekEnergy = thisWeek
		thisMonth = thisMonthStart + todayEnergy
		thisMonthTotal = thisMonthTotal + thisMonth
		device.thisMonthEnergy = thisMonth
	}
	state.todayTotalEnergy = todayTotalEnergy
	state.thisWeekTotal = thisWeekTotal
	state.thisMonthTotal = thisMonthTotal
	logDebug "Energy update done"
	updateCost()
}

void updateCost(){
	logDebug "Start cost update"
	def totalCostToday = 0
	def totalCostWeek = 0
	def totalCostMonth = 0
	energies.each {dev ->
		device = state.energies["$dev.id"]
		todayVar = device.todayVar
		weekVar = device.weekVar
		monthVar = device.monthVar
		thisWeek = device.thisWeekEnergy
		thisMonth = device.thisMonthEnergy
		tempEnergy = new BigDecimal(device.todayEnergy)
		tempRate = new BigDecimal(state.energyRate)
		BigDecimal tempCost = tempEnergy*tempRate
		BigDecimal tempWeekCost = thisWeek*tempRate
		BigDecimal tempMonthCost = thisMonth*tempRate
		tempCost = tempCost.setScale(2, BigDecimal.ROUND_HALF_DOWN)
		tempWeekCost = tempWeekCost.setScale(2, BigDecimal.ROUND_HALF_DOWN)
		tempMonthCost = tempMonthCost.setScale(2, BigDecimal.ROUND_HALF_DOWN)
		totalCostToday = totalCostToday + tempCost
		totalCostWeek = totalCostWeek + tempWeekCost
		totalCostMonth = totalCostMonth + tempMonthCost
		if(symbol) {device.todayCost = symbol+tempCost.toString()} else {device.todayCost = tempCost.toString()}
		if(symbol) {device.thisWeekCost = symbol+tempWeekCost.toString()} else {device.thisWeekCost = tempWeekCost.toString()}
		if(symbol) {device.thisMonthCost = symbol+tempMonthCost.toString()} else {device.thisMonthCost = tempMonthCost.toString()}
		logTrace "Table Refresh: New cost for ${dev.displayName} is ${device.todayCost}"
		if(todayVar) setGlobalVar(todayVar, device.todayCost)
		if(weekVar) setGlobalVar(weekVar, device.thisWeekCost)
		if(monthVar) setGlobalVar(monthVar, device.thisMonthCost)
	}
	todayTotalVar = state.todayTotalVar
	weekTotalVar = state.weekTotalVar
	monthTotalVar = state.monthTotalVar
	if(symbol) {state.totalCostToday = symbol+totalCostToday.toString()} else {state.totalCostToday = totalCostToday.toString()}
	if(symbol) {state.totalCostWeek = symbol+totalCostWeek.toString()} else {state.totalCostWeek = totalCostWeek.toString()}
	if(symbol) {state.totalCostMonth = symbol+totalCostMonth.toString()} else {state.totalCostMonth = totalCostMonth.toString()}
	if(todayTotalVar) setGlobalVar(todayTotalVar, state.totalCostToday)
	if(weekTotalVar) setGlobalVar(weekTotalVar, state.totalCostWeek)
	if(monthTotalVar) setGlobalVar(monthTotalVar, state.totalCostMonth)
	logDebug "Cost update done"
}

void dayStartEnergy() {
	logDebug "Daily reset"
	energies.each {dev ->
		device = state.energies["$dev.id"]
		if(dev.currentEnergy) {energy = dev.currentEnergy} else {energy = 0}
		logTrace ""
		device.dayStart = energy
		device.weekStart = device.thisWeekEnergy
		device.monthStart = device.thisMonthEnergy
		logDebug "${dev} starting energy is ${device.dayStart}"
	}
}

void resetWeekly() {
	logDebug "Weekly reset"
	energies.each {dev ->
	device = state.energies["$dev.id"]
	device.lastWeekEnergy = 0
	device.lastWeekEnergy = device.thisWeekEnergy
	device.thisWeekEnergy = 0
	}
	state.lastWeekTotal = state.thisWeekTotal
	state.thisWeekTotal = 0
}

void resetMonthly() {
	logDebug "Monthly reset"
	energies.each {dev ->
	device = state.energies["$dev.id"]
	device.lastMonthEnergy = 0
	device.lastMonthEnergy = device.thisMonthEnergy
	device.thisMonthEnergy = 0
	}
	state.lastMonthTotal = state.thisMonthTotal
	state.thisMonthTotal = 0
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
    }
}

def logTrace(msg) {
    if (settings?.traceOutput) {
		log.trace msg
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    app.updateSetting("debugOutput",[value:"false",type:"bool"])
}