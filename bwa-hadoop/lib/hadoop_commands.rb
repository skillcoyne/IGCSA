require 'yaml'

class HadoopCommands
  @@hadoop_dfs = "bin/hadoop dfs"

  def initialize(hadoop_path, hdfs_path)
    @hadoop_path = hadoop_path
    @hdfs_path = hdfs_path
  end

  def copy_to_hdfs(localpath, opts = {})
    copy_path = get_path(opts)

    basename = File.basename("#{localpath}")
    file_exists = list(:path => "#{copy_path}/#{basename}")
    puts "#{copy_path}/#{basename} exists: #{file_exists}"
    if opts[:overwrite]
      puts "overwriting #{file_exists}"
      remove_from_hdfs(basename, opts) if file_exists
    else
      return file_exists
    end

    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -copyFromLocal #{localpath} #{copy_path}/#{basename}"
    `#{cmd}`
    unless $?.success?
      $stderr.puts "Command failed: #{cmd}: #{$?}"
      exit(-1)
    end

    return "#{copy_path}/#{basename}"

  end


  def copy_from_hdfs(file, localpath, opts = {})
    path = get_path(opts)
    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} --copyToLocal #{path}/#{file} #{localpath}/#{file}"
    puts cmd
    return `#{cmd}`
  end

  def remove_from_hdfs(file, opts = {})
    path = get_path(opts)
    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -rmr #{path}/#{file}"
    puts cmd
    output = `#{cmd}`
    unless $?.success?
      $stderr.puts "Command failed: #{cmd}: #{$?}"
      exit(-1)
    end
    return output
  end


  def list(opts = {})
    path = get_path(opts)

    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -ls #{path}"
    output = `#{cmd}`
    unless $?.success?
      return nil
    end

    files = output.split("\n")
    files = files[1..files.length]
    files.map! { |e|
      es = e.split("\s")
      es[es.length-1]
    }
    return files
  end

  :private

  def get_path(opts = {})
    if opts[:path]
      if opts[:path].start_with? "/"
        return opts[:path]
      else
        return "#{@hdfs_path}/#{opts[:path]}"
      end
    end
    return @hdfs_path
  end

end