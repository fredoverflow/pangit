![pangit](pangit.png)

## Background

Have you ever staged some files in git, forgot to commit them and then messed something up?
Fear not, staged files are usually recoverable within a certain time frame!

pangit lets you browse all recently staged files, even uncommitted ones, sorted by most recently staged.
(Doing this manually with git plumbing commands is possible, but tedious.)

## Does the name mean anything?

"pangit" is an amalgamation of "panic" and "git".
I pronounce it like "dangit", but with the first letter rotated 180° ;)

Apparently, "pangit" means "ugly" in [Tagalog](https://en.wikipedia.org/wiki/Tagalog_language).
I wasn't aware of that when I picked the name, but given [git's own etymology](https://en.wikipedia.org/wiki/Git#Naming), I'm fine with it.

## How do I compile pangit into an executable jar?
```
git clone https://github.com/fredoverflow/pangit
cd pangit
mvn package
```
The executable `pangit.jar` will be located inside the `target` folder.
