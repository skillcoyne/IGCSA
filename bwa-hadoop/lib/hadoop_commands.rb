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

    if opts[:overwrite]
      puts "overwriting #{file_exists}"
      remove_from_hdfs(basename, opts) if file_exists
    else
      return file_exists
    end

    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -copyFromLocal #{localpath} #{copy_path}/#{basename}"
    puts cmd
    `#{cmd}`
    unless $?.success?
      $stderr.puts "Command failed: #{cmd}: #{$?}"
      exit(-1)
    end

    return "#{copy_path}/#{basename}"

  end


  def copy_from_hdfs(file, localpath, opts = {})
    path = get_path(opts)
    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -copyToLocal -ignoreCrc #{file} #{localpath}/#{File.basename(file)}"
    puts cmd
    return `#{cmd}`
  end


  def remove_from_hdfs(file, opts = {})
    path = get_path(opts)

    file = "#{path}/#{file}" unless file.start_with? "/"

    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -rmr #{file}"
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
    $stderr.puts "Listing #{path}"
    cmd = "#{@hadoop_path}/#{@@hadoop_dfs} -ls #{path}"
    output = `#{cmd}`
    unless $?.success?
      return nil
    end

    files = output.split("\n")
    if files.length > 0
      files = files[1..files.length]
      files.map! { |e|
        es = e.split("\s")
        es[es.length-1]
      }
      files.delete_if {|e| File.basename(e).start_with?"_"}
    end
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