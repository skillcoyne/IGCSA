require 'logger'

module Logging
  @out = STDOUT
  # This is the magical bit that gets mixed into your classes
  def log
    Logging.log
  end

  def configure(config)
    logout = config['logout']
    if logout != 'STDOUT'
      @out = logout # should be a log path, like /tmp/log.txt
    end
  end

  # Global, memoized, lazy initialized instance of a logger
  def self.log
    @log ||= Logger.new(@out)
  end

end