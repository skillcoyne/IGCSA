require 'yaml'

module GFF


  def self.set_regions(r)
    @regions = r
  end

  def self.get_regions
    @regions
  end

  class GVFReader
    attr_reader :regions

    def initialize(file, chr = [])
      @gvf = file
      regions = {}
      @fh = File.open(@gvf, 'r')
      @fh.each_with_index do |line, index|
        line.chomp!
        get_date(line) if index.eql? 2
        get_build(line) if index.eql? 3

        if line.match(/sequence-region/)
          (sr, name, start, stop) = line.split(/\s/)
          if !chr.empty?
            (regions[name] ||= []) << [start, stop] if chr.index(name)
          else
            (regions[name] ||= []) << [start, stop]
          end
        end

        break unless line.start_with? '#'
      end
      GFF.set_regions(regions)
    end

    def read_line
      gvf = nil
      begin
      line = @fh.readline
      gvf =  GVF.new(line, @fh.lineno)
      rescue EOFError => e
        puts "End of file reached"
      end
      return gvf
    end

    def close
      @fh.close
    end

    :private

    def get_date(line)
      line.match(/(\d{4}-\d+-\d+)/)
      @date = $1
    end

    def get_build(line)
      line.match(/ensembl (GRCh\d+)/)
      @build = $1
    end


  end

  class GVF
    attr_reader :chr, :source, :method, :start_pos, :end_pos, :score, :strand, :phase, :attributes, :other, :line_num

    def initialize(line, linenum)
      @line_num = linenum
      cols = line.split("\t")
      if GFF.get_regions
        read(cols) if GFF.get_regions[cols[0]]
      else
        read(cols)
      end
    end

    :private
    def read(cols)
			cols.map!{|e| e.strip }
      (@chr, @source, @method, sp, ep, @score, @strand, @phase, @attributes) = cols[0..8]
      @other = cols[9..cols.length]
      feature_attributes()

      @score = nil if @score.eql? '.'
      @phase = nil if @phase.eql? '.'

      @start_pos = sp.to_i
      @end_pos = ep.to_i
    end

    def feature_attributes
      attr = {}
      @attributes.split(/;/).each { |a|
        (name, value) = a.split(/=/)

        name = "validation" if name.downcase.match(/validation/)

        if name.downcase.match(/dbxref/)
          xref = value.split(/:/)
          attr[xref[0].downcase.to_sym] = xref[1]
        else
          attr[name.downcase.to_sym] = value
        end
      }
      @attributes = attr
    end

  end

end
