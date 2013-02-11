require 'yaml'
require 'fileutils'

class Utils

  def self.date
    time = Time.new
    return time.strftime("%d%m%Y")
  end

  def self.check_config(cfg_file, cfg_def, optional_keys = [])
    puts "Using #{cfg_file} config file"
    cfg = YAML.load_file(cfg_file)

    optional_keys.each { |k| cfg_def.delete(k); cfg.delete(k) }
    if cfg.keys.sort!.eql? cfg_def.keys.sort!
      return YAML.load_file(cfg_file)
    else
      puts "Incorrect config file, expected keys:\n"
      puts YAML::dump cfg_def
      exit(1)
    end
  end


  def self.setup_dirs(dirs = [], regen=false)
    dirs.each do |dir|
      FileUtils.rm_f(dir) if (regen and File.exists?dir)
      FileUtils.mkpath(dir)
    end
  end


end
