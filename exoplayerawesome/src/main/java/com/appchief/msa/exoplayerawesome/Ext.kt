package com.appchief.msa.exoplayerawesome

fun String?.encodeUrl(): String {
	 return this?.replace(" ", "%20") ?: ""
}