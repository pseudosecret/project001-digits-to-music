# Why

My unfiltered opinion: the artistry in data sonification is less in the notes produced, and more in the rules that determine how the notes are produced.

Data (unless the data is literally sound) does not sound like anything at all. The number “one” does not sound like anything, nor does a string of characters forming a URL. The only way data “sounds” like anything is if it is translated through an abstract system into sound, and even then, what is heard is not the data, merely a translation of the data.

This humble project is my first attempt at taking a sequence of numbers and making meaningful sounds out of them. My motivation was purely out of personal interest, although I fell in love with the process. I like the way my brain feels when I am working on this, so with any luck, I will be able to find a way to devote more of my time and life to it.


## Iteration One

Here are the rules in play that create music based on a bigdecimal number for the first iteration: 

I use a sampled piano patch because I like the way it sounds. :)

Each digit of the number is mapped to a scale degree, e.g., (0 1 2 3 4 5 6 7 8 9) → tonic, supertonic, mediant, subdominant, dominant, submediant, subtonic, tonic, supertonic). While some who have tackled similar algorithmic composition before me have tend to map 0 → tonic, my version surrounds the scale with its dominant chord, and I think it better matches how the pitches in a scale are typically described, e.g., having 0 be the 1st scale degree does not match our language as well as having 1 be the 1st scale degree. 

The harmony uses a different system altogether. I did this so I could potentially have a greater range of notes explored, and keep something sounding at least roughly like a heavily colored and flavored chord. The first harmonic major scale that I use is the 1st, 2nd, 3rd, 5th, 6th, 7th, 8th, 9th, 10th, and 12th degrees—or something like that.

In my first iteration, there are no transitions from one scale form to another.

For the melody: in the interest of aesthetics, if the digits as converted to scale degrees are greater than a specified interval, then the note played is either increased or decreased by an octave, whichever would make it closer to the preceding note. This is meant to happen within a specified range, as the sampled-piano patch only produces sound if an integer 20 to 110 or so is passed, making some octave like A#28 not acceptable.

Also, in order to handle melody note length, a digit's length is determined by how likely the subsequent digit is to occur. If the subsequent digit is “unlikely” (e.g., greater than 0.8 standard deviations from the mean in frequency) to occur, then the note's duration is increased. It may be worth mentioning that the values I use to determine what is considered “unusual” are arbitrary (I tweaked what percentage of a standard deviation is in use to taste, with Pi as the basis).

The number is split into segments: whenever a number occurs three times in a row, a new sublist is created that continues until either the next triple or the end of the list, e.g., ((0 1 2) (3 3 3 4) (5 5 5 6 7)). The primary function iterates over these sublists, and uses the material within (i.e., the digits) in order to create the melody and harmony. At present, the only change from one sublist to the next is that the base duration is decreased by 5ms.

When a double occurs, the harmony changes chords to whichever the new “root” is. For example, if the harmonic-scale in is at pitch value 40, and the double occurs at pitch value 48, then the total scale is moved up by eight semi-tones. That said, this is set within a limit, which prevents the pitch-adjusting value from ever exceeding the maximum or minimum values.

I think this provides a fair synopsis of what I have done. I hope what you hear is at least somewhat palatable.


## Iteration Two: wishlist

When I wrote my little entry for iteration one, that was after I had already written the code. I had to review it a little bit to make sure I understood exactly what I did (I wrote it about a week-ish after I had finished it), but iteration two I have not yet started. As such, this will partly function as a checklist for me.
* First thing I need to do is maybe come up with strategies to create varieties in texture that do not feel random. So I guess this is just me brain-storming for a bit:
* If two adjacent sublists would play for approximately the same duration, then this could trigger a mirrored approach.
For instance, if the duration is ten seconds for one and ten and a half for the following, then that would be “close enough”. The melody would first be in the upper register, and the harmony in the lower register.  For the second sublist, they would flip: the melody would be in the lower register, harmony would be in the upper register.
* If the second of two adjacent sublists has less than 10 (or some other arbitrary number—I will give it some thought), then the harmony and melody patterns used with the second sublist will be the same as the first.
* Whether the harmony is major or minor (and, by consequence, the melody) can be determined by whether something is more or less than the mean (or, maybe the mean minus some percentage of the standard deviation?). Personally, I prefer minor to major tonalities, but from what I witnessed with iteration one, the tonality is going to be fairly dark regardless of what I do, which is fine by me. There will be a threshold, though—as stated above, if the number of digits in a sublist is below a certain amount, then it will just copy the previous.
* It would also be nice to adjust the play-melody-segment so that when the melody reaches a certain pitch-adjust value, it plays the notes at the normal value and at an octave lower.
* … more to come.

