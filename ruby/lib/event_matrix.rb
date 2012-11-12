require 'yaml'

class EventMatrix
  @@header_col = 0

  class << self
    attr_accessor :event_indecies, :event_matrix
  end

  def initialize()
    @event_indecies = {}
    @event_matrix = []
    @event_matrix[@@header_col] = Array.new

  end

  def matrix
    matrix = @event_matrix[1, @event_matrix.length]
  end

  def events
    return @event_matrix[@@header_col]
  end

  def output(file)
    file.write("\t" + self.events.join("\t") + "\n")
    self.matrix.each_with_index do |column, i|
      file.write("#{self.events[i]}\t" + column.join("\t") + "\n")
    end
  end


  # linked events
  def link_events(le)
    # add each event individually if they don't already exist
    le.each do |e|
      next if @event_indecies.has_key? e
      add_event(e)
    end

    # go back through and add to the matrix for each linked event
    le.each do |e|

      (col, row) = @event_indecies[e]

      le.each_with_index do |link, i|
        next if e.eql? link # skip current event

        (lcol, lrow) = @event_indecies[link]

        @event_matrix[lrow][col] += 1
      end
    end

    #@event_matrix.each_with_index do |column, i|
    #  puts "#{i}:  #{column.join(', ')} "
    #end
  end

  def add_event(*args)
    e = args[0]
    (args.length == 2) ? count = args[1] : count = 1


    if @event_indecies.has_key?(e)
      (col, row) = @event_indecies[e]
      @event_matrix[row][col] += count

    else
      @event_matrix[@@header_col].push(e)
      col = @event_matrix[@@header_col].length - 1

      @event_matrix.each_with_index do |row, i| # map won't work b/c you have to skip the first row
        next if i == 0
        row[col] = 0
      end

      new_row = Array.new(@event_matrix[@@header_col].length, 0)
      new_row[col] = count # event

      @event_matrix.push(new_row)
      row = @event_matrix.length - 1

      @event_indecies[e] = [col, row]
    end

    #@event_matrix.each_with_index do |column, i|
    #  puts "#{i}:  #{column.join(', ')} "
    #end
  end


end