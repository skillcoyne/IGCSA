read_alignment<-function(brg)  # takes bamRange
  {
  #print(paste( mateReverseStrand(align), insertSize(align), sep="  " ) )
  
  rewind(brg)
  pp = vector(mode="integer")
  dc = vector(mode="integer")
  
  align <- getNextAlign(brg)
  while(!is.null(align))
    {
    #if (!unmapped(align) & !mateUnmapped(align) & insertSize(align) != 0) 
      #{
      message( paste(name(align), position(align), sep=" " )
               
      #if ( paired(align) & !failedQC(align) & !pcrORopt_duplicate(align) )
        #{
        #if (properPair(align) & !secondaryAlign(align))  
          #pp = append( pp, abs(insertSize(align)) )
        #else if ( refID(align) == mateRefID(align) ) 
          #dc = append( dc, abs(insertSize(align)) ) # inter-chr
        #}
      #}
  align = getNextAlign(brg)
    }
  #return( list("ppair" = pp, "disc" = dc)  )
  }
