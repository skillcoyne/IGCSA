require 'massive_record'
require 'yaml'

conn = MassiveRecord::Wrapper::Connection.new(:host => 'localhost', :port => 9090)
conn.open

#client = conn.client


puts conn.tables
table = MassiveRecord::Wrapper::Table.new(conn, "users")

#table.column_families.push( column = MassiveRecord::Wrapper::ColumnFamily.new(:info) )
#table.create_column_families([:friends, :misc])
#
#table.save

puts table.column_families

#puts YAML::dump table.first
#
#
table.fetch_column_families.each do |f|
  puts f.name
end


#puts client.getTableNames
#
#puts "**********"
#
#puts YAML::dump client.getColumnDescriptors("genome")
#
#puts client.getRow("genome", "igcsa7")


