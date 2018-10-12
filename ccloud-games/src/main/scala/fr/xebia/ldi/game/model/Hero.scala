package fr.xebia.ldi.game.model

import fr.xebia.ldi.common.schema.{Hero => JHero}
/**
  * Created by loicmdivad.
  */
case class Hero(name: String, fullName: Option[String], creation: Int, comic: String, hit: Option[String]){

  def toJava: JHero = new JHero(name, fullName.orNull, creation, comic, hit.orNull)
}

object Hero {

  def apply(c: JHero): Hero = new Hero(
    name = c.getName,
    fullName = Option(c.getFullName),
    creation = c.getCreation,
    comic = c.getComic,
    hit = Option(c.getHit)
  )

  val Wolverine = Hero("wolverine", Some("Logan"), 1930, "X-MEN", Some("F"))
  val SpiderMan = Hero("spider-man", Some("Logan"), 1930, "X-MEN", Some("F"))
  val IronMan = Hero("iron-man", Some("Logan"), 1930, "X-MEN", Some("F"))
  val AntMan = Hero("ant-man", Some("Logan"), 1930, "X-MEN", Some("F"))

  val Captain = Hero("captain-america", Some("Steven Rogers"), 1960, "", Some(""))
  val Hawkeye = Hero("Hawkeye", Some("Clinton Francis"), 1960, "", Some(""))

/*  Recrues des Années 1960
  Œil-de-Faucon (Hawkeye) / Clinton Francis « Clint » Barton
  Vif-Argent (Quicksilver) / Pietro Maximoff
    Sorcière Rouge (Scarlet Witch) / Wanda Maximoff
    Swordsman / Jacques Duquesne
    Hercule (Hercules) / Héraclès
  La Panthère noire (Black Panther) / T'Challa Udaku
  Vision / Victor Shade
    Chevalier noir (Black Knight) / Dane Whitman
    Recrues des Années 1970
  La Veuve noire (Black Widow) / Natasha Romanoff ou Natalia Romanova
  Mantis
  Le Fauve (Beast) / Henry Phillip « Hank » McCoy
  Dragon-lune (Moondragon) / Heather Douglas ou Kamaria
    Hellcat / Patricia « Patsy » Walker
  Two-Gun Kid / Matthew J. « Matt » Hawk
  Miss Marvel / Carol Danvers
  Le Faucon (Falcon) / Samuel Thomas « Sam » Wilson
  Recrues des Années 1980
  Wonder Man / Simon Williams
  Tigra / Greer Grant Nelson
  Miss Hulk (She-Hulk) / Jennifer « Jen » Walters
  Captain Marvel / Monica Rambeau
  Starfox / Eros
  Namor / Namor McKenzie
    Docteur Druid / Anthony Ludgate Druid*/


  val Heroes: Vector[Hero] = Vector(Wolverine, SpiderMan, IronMan, AntMan, Captain, Hawkeye)

}
