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
}

def mainPage() {
    
	if(state.energies == null) state.energies = [:]
	if(state.energiesList == null) state.energiesList = []
	dynamicPage(name: "mainPage", uninstall: true, install: true) {
		section("<b>App Name</b>",hideable: true, hidden: true) {
            label title: "<b>***Enter a name for this app.***</b>", required:true, width: 4
        }
        section("<b>Energy Rate Information</b>",hideable: true, hidden: true) {
			input "energyRate", "string", title: "What is your energy rate per kWh?", required: false, default: 1, width: 4, submitOnChange: true
			if(!energyRate) {state.energyRate = 1} else {state.energyRate = energyRate}
            input "symbol", "string", title: "What is your currency symbol? (Optional)", required: false, width: 4, submitOnChange: true
		}
        section("<b>Device Selection</b>",hideable: true, hidden: true) {
			input "energies", "capability.energyMeter", title: "Select Energy Devices to Measure Cost", multiple: true, submitOnChange: true, width: 4
		}
		section{
            
			energies.each {dev ->
				if(!state.energies["$dev.id"]) {
					state.energies["$dev.id"] = [energy: dev.currentEnergy, total: 0, var: ""]
                    if(!state.energies["$dev.id"].energy) {state.energies["$dev.id"].energy = 0}
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
				if(state.newVar) {
					List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
					input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
					if(newVar) {
						state.energies[state.newVar].var = newVar
						state.remove("newVar")
						app.removeSetting("newVar")
						paragraph "<script>{changeSubmit(this)}</script>"
					}
				} else if(state.remVar) {
					state.energies[state.remVar].var = ""
					state.remove("remVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				} else if(state.addTotalVar) {
					List vars = getAllGlobalVars().findAll{it.value.type == "string"}.keySet().collect().sort{it.capitalize()}
					input "newVar", "enum", title: "Select Variable", submitOnChange: true, width: 4, options: vars, newLineAfter: true
					if(newVar) {
						state.totalVar = newVar
						state.remove("addTotalVar")
						app.removeSetting("newVar")
						paragraph "<script>{changeSubmit(this)}</script>"
					}
				} else if(state.remTotalVar) {
					state.totalVar = ""
					state.remove("remTotalVar")
					paragraph "<script>{changeSubmit(this)}</script>"
				}
				input "refresh", "button", title: "Refresh Table", width: 2
                input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: false, displayDuringSetup: false, required: false
			}
		}
	}
}

String displayTable() {
	String str = "<script src='https://code.iconify.design/iconify-icon/1.0.0/iconify-icon.min.js'></script>"
	str += "<style>.mdl-data-table tbody tr:hover{background-color:inherit} .tstat-col td,.tstat-col th { padding:8px 8px;text-align:center;font-size:12px} .tstat-col td {font-size:15px }" +
		"</style><div style='overflow-x:auto'><table class='mdl-data-table tstat-col' style=';border:2px solid black'>" +
		"<thead><tr style='border-bottom:2px solid black'><th style='border-right:2px solid black'>Meter</th>" +
		"<th>Energy Consumption</th>" +
		"<th>Cost</th>" +
		"<th>Variable</th></tr></thead>"
	updateCost()
	energies.sort{it.displayName.toLowerCase()}.each {dev ->
		String thisVar = state.energies["$dev.id"].var
		String energy = state.energies["$dev.id"].energy
        String cost = state.energies["$dev.id"].cost
		String devLink = "<a href='/device/edit/$dev.id' target='_blank' title='Open Device Page for $dev'>$dev"
		String var = thisVar ? buttonLink("r$dev.id", thisVar, "purple") : buttonLink("n$dev.id", "Select", "green")
		str += "<tr style='color:black'><td style='border-right:2px solid black'>$devLink</td>" +
			"<td style='color:${"green"}'>$energy</td>" +
			"<td title='Money spent running $dev'>$cost</td>" +
			"<td title='${thisVar ? "Deselect $thisVar" : "Set a String Hub Variable to cost value"}'>$var</td></tr>"
	}
	String totalVar = state.totalVar
	logDebug "totalVar is ${totalVar}"
    String totesVar = totalVar ? buttonLink("remTotal", totalVar, "purple") : buttonLink("varTotal", "Select", "green")
	String totalEnergy = state.totalEnergy
	String totalCost = state.totalCost
    str += "<tr style='color:black'><td style='border-right:2px solid black'>Total</td>" +
			"<td style='color:${"green"}'>$totalEnergy</td>" +
			"<td title='Money spent running $dev'>$totalCost</td>" +
			"<td title='${totalVar ? "Deselect $totalVar" : "Set a String Hub Variable to cost value"}'>$totesVar</td></tr>"
	str += "</table></div>"
	str
}

String buttonLink(String btnName, String linkText, color = "#1A77C9", font = "15px") {
	"<div class='form-group'><input type='hidden' name='${btnName}.type' value='button'></div><div><div class='submitOnChange' onclick='buttonClick(this)' style='color:$color;cursor:pointer;font-size:$font'>$linkText</div></div><input type='hidden' name='settings[$btnName]' value=''>"
}

void appButtonHandler(btn) {
	logDebug "btn is ${btn}"
	if(btn == "refresh") state.energies.each{k, v ->
		def dev = energies.find{"$it.id" == k}
		if(!dev.currentEnergy) {
            state.energies[k].energy = 0
        } else
            state.energies[k].energy = dev.currentEnergy
	} else if(btn == "varTotal") state.addTotalVar = btn
	else if(btn == "remTotal") state.remTotalVar = btn
	else if(btn.startsWith("n")) state.newVar = btn.minus("n")
	else if(btn.startsWith("r")) state.remVar = btn.minus("r")
	else state.reset = btn.minus("d")
}

def updated() {
	unsubscribe()
	initialize()
}

def installed() {
}

void initialize() {
	subscribe(energies, "energy", energyHandler)
}

void energyHandler(evt) {
    logDebug "Energy change for ${evt.device}"
	updateCost()
}

void updateCost() {
	def totalEnergy = 0
    def totalCost = 0
	energies.sort{it.displayName.toLowerCase()}.each {dev ->
		String thisVar = state.energies["$dev.id"].var
		if(dev.currentEnergy) {energy = dev.currentEnergy} else {energy = 0}
		logDebug "${dev} energy is ${energy}"
		totalEnergy = totalEnergy + energy
        BigDecimal tempEnergy = new BigDecimal(energy)
        BigDecimal tempRate = new BigDecimal(state.energyRate)
        BigDecimal tempCost = tempEnergy*tempRate
        tempCost = tempCost.setScale(2, BigDecimal.ROUND_HALF_DOWN)
        totalCost = totalCost + tempCost
        if(symbol) {state.energies["$dev.id"].cost = symbol+tempCost.toString()} else {state.energies["$dev.id"].cost = tempCost.toString()}
        logDebug "Table Refresh: New cost for ${dev.displayName} is ${state.energies["$dev.id"].cost}"
		if(thisVar) setGlobalVar(thisVar, state.energies["$dev.id"].cost)
	}
	String thatVar = state.totalVar
	if(symbol) {totalCost = symbol+totalCost.toString()} else {totalCost = totalCost.toString()}
	state.totalCost = totalCost
	state.totalEnergy = totalEnergy
	if(thatVar) setGlobalVar(thatVar, state.totalCost)
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    app.updateSetting("debugOutput",[value:"false",type:"bool"])
}