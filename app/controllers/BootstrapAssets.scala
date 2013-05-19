package controllers

object BootstrapAssets {
  def at(path: String, file: String) = Assets.at(path, file)
  def image(path: String, file: String) = Assets.at(path, file)
}
