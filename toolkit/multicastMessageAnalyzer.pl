#!/usr/bin/perl
use warnings FATAL => 'all';
use strict;

# Multicast message analyzer: Prints
#
#* Average group message delivery ratio per time
#* Minimum group message delivery ratio per time
#
# Parses the MulticastDeliveryReport to do so, i.e. translates a file of the form
#   #message, sent, received, ratio
#   <MessageId> <SentTime> <RecvdTime> <DelRatio>
#   ...
#   <MessageId> <SentTime> <RecvdTime> <DelRatio>
#   <TotalSimulatorTime>
# where lines are printed after creation and each delivery into a file of the form
#   #SimTime	MinRatio	AvgRatio
#   <time after creation>	<min>	<avg>
#   ...
#
# Messages may not exist anymore at a certain time point. Even if there was an update
# of the ratio after the last interval, it will then not be considered in the next interval.

# Parse command line parameters.
if (not defined $ARGV[0] or not defined $ARGV[1]) {
    print "Usage: <inputFile> <timeStep>\n";
    exit();
}
my $infile = $ARGV[0];
my $timeStep = $ARGV[1];

# Define useful fields:

#Maps intervals (= time after message creation) to maps of messages and their delivery ratio during this interval
my %intervalToAvgs = ();
#Maps a message to the time it was created
my %msgToCreateTime = ();
#Maps a message to the number of intervals between message creation and simulation end.
#This is used to only take messages into account, that existed in the given time interval
my %msgToMaxInterval = ();


# Matches a message line.
my $messageLineMatcher = '^(\D+\d+) (\d+) (\d+) (\d+([.]\d+)?)$';
# Matches the last report line, i.e. the total simulation time.
my $simTimeLineMatcher = '^(\d+([.]\d+)?)$';

# Read multicast report.
open(INFILE, "$infile") or die "Can't open $infile : $!";
while(<INFILE>) {
    # Try to parse lines either of type <msgId> <ctime> <rtime> <ratio> or <simtime>.
    my ($msgId, $createTime, $recvTime, $ratio) = m/$messageLineMatcher/;
    my ($simTime) = m/$simTimeLineMatcher/;

    # Ignore lines that are not of this kind.
    if (not defined $createTime and not defined $simTime) {
        next;
    }

    # Handle sim time lines in a special way.
    if (defined $simTime) {
        # Calculates the highest interval for every node, in which it has to be taken to account
        calculateMaxIntervalForAllMsgs($simTime);
        # Sim time line should be last line.
        last;
    }
    #calculate the interval this message was delivered in
    my $timeInterval = int(($recvTime - $createTime) / $timeStep + 1);

    $msgToCreateTime{$msgId} = $createTime;
    #put the message and its ratio in the map for the calculated interval
    $intervalToAvgs{$timeInterval}{$msgId} = $ratio;
}

close(INFILE);

#Map, that stores the last delivery ratio for every message
my %msgToLastRatio = ();

print "#timeAfterMessageCreation	MinRatio	AvgRatio   MinRationForAll AvgRatioForAll\n";

my $lastInterval = 0;
#Sort intervals numerically and process every interval step by step
foreach my $interval ( sort {$a <=> $b} keys %intervalToAvgs){
    #fill missing intervals with values of prior interval
    while ($lastInterval + 1 < $interval){
        printInterval($lastInterval + 1);
        $lastInterval++;        
    }
    #for every message delivered during this interval, update the latest delivery ratio
    foreach my $msg (keys %{$intervalToAvgs{$interval}}) {
        $msgToLastRatio{$msg} = $intervalToAvgs{$interval}{$msg};
    }
    printInterval($interval);
    $lastInterval = $interval;
}

#fill lines up to simulation end
while (getHighestInterval() > $lastInterval){
    $lastInterval++;
    printInterval($lastInterval);
}



sub getHighestInterval{
    my $max = 0;
    $_ > $max and $max = $_ for values %msgToMaxInterval;
    return $max;
}


#calculates and prints the min and average for the given interval
sub printInterval{
    my $interval = shift;
    my $ratioSum = 0;
    my $ratioSumForAll = 0;
    my $nextMin = 2;
    my $nextMinForAll = 2;
    my $msgCount = 0;
    my $msgCountForAll = 0;
    #check every message
    foreach my $msg (keys %msgToLastRatio){
        #calculate these metrics only for message that still exist during the current interval
        if ($msgToMaxInterval{$msg} >= $interval){
            #add it to the min and avg calculation for the current interval
            $msgCount++;
            my $msgRatio = $msgToLastRatio{$msg};
            $ratioSum += $msgRatio;
            if ($nextMin > $msgRatio){
                $nextMin = $msgRatio;
            }
        }
        #calculate these metrics for all messages ever created
        $msgCountForAll++;
        my $msgRatio = $msgToLastRatio{$msg};
        $ratioSumForAll += $msgRatio;
        if ($nextMinForAll > $msgRatio){
            $nextMinForAll = $msgRatio;
        }

    }
    if ($msgCount > 0) {
        #calculate average
        my $nextAvg = $ratioSum / $msgCount;
        my $nextAvgForAll = $ratioSumForAll / $msgCountForAll;
        #convert the interval into simulation seconds
        my $seconds = $interval * $timeStep;
        print "$seconds	$nextMin	$nextAvg   $nextMinForAll $nextAvgForAll\n";
    }
}

sub calculateMaxIntervalForAllMsgs {
    # Get final simulator time.
    my $simTime = shift;
    
    foreach my $msgId (keys %msgToCreateTime){
        $msgToMaxInterval{$msgId} = int(($simTime - $msgToCreateTime{$msgId}) / $timeStep );
    }
}
