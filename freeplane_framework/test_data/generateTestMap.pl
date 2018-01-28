#=================== Configuration ===================

# $max_node_number - approximate number of nodes to generate
#our $max_node_number = 1000;
our $max_node_number = 10000;
# our $max_node_number = 100000;
#our $max_node_number = 200000;

# $max_level - number of node levels, root node has level 0
our $max_level = 7;

# $max_level - number of child nodes for each node
our $max_number = 7;

# $attributeNumber - number of attributes added to each node
our $attributeNumber = 0;

# $iconNumber - number of icons added to each node
our $iconNumber = 2;

# $linkNumber - number of links added to each node
our $linkNumber = 0;

#$richcontent: 0 - use plain text, 1 - use rich content
our $richcontent = 0;

#$notes: 0 - use plain text, 1 - use notes
our $notes = 1;

#=================== Script ==========================
use strict;
our $counter = 0;
our $rest = 1;

my $mmfile;

open($mmfile,
">map_$max_node_number-$max_level-$max_number-$attributeNumber-$linkNumber-$richcontent-$notes.mm");

sub attributes($)
{
 my $level = shift;
 for(my $i = 1; $i <= $attributeNumber; $i++)
 {
       print $mmfile " " x ($level * 4);
       print $mmfile "<attribute NAME=\"$i\" VALUE=\"attribute $i $i
$i\"/>\n";
 }
}

sub icons($)
{
    my $level = shift;
    for(my $i = 0; $ i < $iconNumber; $i++)
    {
        my $j = ($i + $level-1) % 9 + 1;
	print $mmfile " " x ($level * 4);
        print $mmfile "<icon BUILTIN=\"full-" . $j . "\"/>\n";
    }
}

sub links($)
{
 if(! $linkNumber)
  {
       return;
  }
 my $level = shift;
 for(my $i = 1; $i <= $attributeNumber; $i++)
 {
   my $target = int(rand($max_node_number)) + 1;
   if($target == $counter)
   {
       $target--;
   }
   print $mmfile " " x ($level * 4);
   print $mmfile "<arrowlink DESTINATION=\"Freemind_Link_$target\"
ENDARROW=\"Default\" ENDINCLINATION=\"49;0;\" STARTARROW=\"None\"
STARTINCLINATION=\"49;0;\"/>\n";
 }
}

sub print_richcontent($$$)
{
    my $contentType = shift;
    my $content = shift;
	my $space = shift;
 	print $mmfile <<END;
$space<richcontent TYPE=\"$contentType\">
$space<html>
$space  <head>
$space  </head>
$space  <body>
$space    <p>
$space      $content
$space    </p>
$space  </body>
$space</html>
$space</richcontent>
END
}

sub nodes($)
{
 if($counter + $rest > $max_node_number)
 {
       return;
 }
 my $level = shift;
 if ($level > $max_level)
 {
       return;
 }
 $rest += $max_number;
 for(my $i = 1; $i <= $max_number; $i++)
 {
 $counter++;
 $rest--;

 my $folded;
  if($counter + $rest > $max_node_number || $level == $max_level) {
    $folded = '';
  }
  else{
    $folded='FOLDED = "true"';
  }

 my $space = " " x ($level * 4);
 print $mmfile "$space<node ID=\"Freemind_Link_$counter\" ";
 if(! $richcontent)
 {
       print $mmfile "TEXT=\"testnode $level $i $counter\" ";
 }
 print $mmfile "$folded>\n";
 if($richcontent)
 {
	print_richcontent("NODE", "testnode $level $i $counter", $space);
 }
 if($notes)
 {
	print_richcontent("NOTE", "note $level $i $counter", $space);
 }
 attributes($level + 1);
 links($level + 1);
 icons($level + 1);
 nodes($level + 1);
 print $mmfile " " x ($level * 4);
 print $mmfile "</node>\n";
}
 }
my $rootNodeText = "our \$max_node_number = $max_node_number; # approximate
number of nodes to generate&#xa;"
       ."our \$max_level = $max_level; # number of node levels, root node has level 0&#xa;"
       ."our \$max_number = $max_level; # number of child nodes for each node&#xa;"
       ."our \$attributeNumber = $attributeNumber; # number of attributes added to each node&#xa;"
       ."our \$linkNumber = $linkNumber; #number of links added to each node&#xa;"
       ."our \$richcontent = $richcontent; # 0 - use plain text, 1 - use rich content&#xa;"
       ."our \$notes = $notes; # 0 - no notes, 1 - generate notes";
print $mmfile <<ENDOFMAP;
<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from
http://freemind.sourceforge.net -->
<node TEXT=\"$rootNodeText\">
ENDOFMAP
nodes(1);
print $mmfile <<ENDOFMAP2;
</node>
</map>
ENDOFMAP2

