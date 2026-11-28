package com.schoolday.app
import org.junit.Assert.*
import org.junit.Test
class NameTest{
 @Test fun options(){
  assertEquals(listOf("Muhammad","Aiman","Hakim","Muhammad Aiman","Muhammad Hakim","Aiman Hakim"),SchooldayNames.options("Muhammad Aiman Hakim"))
  assertEquals(listOf("王小明"),SchooldayNames.options("王小明"))
  assertFalse("Sara Queen" in SchooldayNames.options("Nur Sara Amira"))
 }
}
