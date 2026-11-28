package com.schoolday.app
object SchooldayNames{
    fun options(fullName:String):List<String>{
        val words=fullName.split(Regex("[\\p{Z}\\s]+")).filter{it.isNotBlank()}
        return (words+words.flatMapIndexed{index,word->words.drop(index+1).map{next->word+" "+next}}).distinct()
    }
}
