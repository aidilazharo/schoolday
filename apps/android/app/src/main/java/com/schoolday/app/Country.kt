package com.schoolday.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import java.text.Collator

object SchooldayCountry{
    val codes="AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW".split(" ")
    fun normalize(value:String?)=if(value in codes)value!! else "MY"
    fun name(code:String,lang:String)=Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.forLanguageTag(if(lang=="zh")"zh-Hans" else lang)).ifBlank{code}
}
@Composable fun CountryMenu(country:String,lang:String,onChange:(String)->Unit){
    var expanded by remember{mutableStateOf(false)}
    var search by remember{mutableStateOf("")}
    val options=remember(lang){val collator=Collator.getInstance(Locale.forLanguageTag(lang));SchooldayCountry.codes.map{it to SchooldayCountry.name(it,lang)}.sortedWith{a,b->collator.compare(a.second,b.second)}}
    OutlinedButton(onClick={search="";expanded=true},modifier=Modifier.fillMaxWidth()){
        Text(SchooldayLanguage.text("Country / region",lang)+" · "+SchooldayCountry.name(country,lang))
    }
    if(expanded) AlertDialog(
        onDismissRequest={expanded=false},
        title={Text(SchooldayLanguage.text("Country / region",lang))},
        text={Column{
            OutlinedTextField(search,{search=it},label={Text(SchooldayLanguage.text("Search country / region",lang))},singleLine=true,modifier=Modifier.fillMaxWidth())
            LazyColumn(Modifier.heightIn(max=320.dp)){
                items(options.filter{it.first.contains(search,true)||it.second.contains(search,true)},key={it.first}){(code,name)->
                    TextButton(onClick={onChange(code);expanded=false},modifier=Modifier.fillMaxWidth()){Text(name+" ("+code+")",modifier=Modifier.weight(1f));if(country==code)Text("✓")}
                }
            }
        }},
        confirmButton={TextButton(onClick={expanded=false}){Text(SchooldayLanguage.text("Done",lang))}}
    )
}
