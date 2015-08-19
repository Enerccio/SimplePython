About `future` and related statements
=====================================

SimplePython adds simplified version of implicit [future construct](https://en.wikipedia.org/wiki/Futures_and_promises)
known from other languages as simple way to do two (or more) things at once.

Using future is made possible by three additions to language:

## future statement

Future statement defines expression that will be computed parallely to current
thread. Statement returns _Future object_ that should be assigned to variable.

Any later operation with _Future object_ (including assigning to other variable)
will result in automatic substitution of _Future object_ with computed value of
original expression. If value is not yet computed, thread will be suspended
until required value is available. That is why only meaningful way to use
Future statement is to assign it to variable:

```
lines = future : file("data.txt").read().split("\n")
```

Above statement will load and split 'data.txt' in parallel thread. Next time when
_lines_ variable will be accessed in any way, it will contain array with contents
of file splited by newline character.

## ready operator

Of course, launching computation in parallel and hoping that it will be done on
time would allow very limited use. That where _ready_ operator comes into place.

```
if ready lines:
    do_something_fancy(lines)
```

ready operator returns `true`, if variable contains _Future object_ created by
[future statement](#future-statement) and computation of expression is done, or
variable doesn't contain _Future object_ at all. For _Future object_ with value
still being computed, operator returns false.

Another, more meaningful example of usage would be:

```
lines = future : file("data.txt").read().split("\n")
while not ready lines:
    # keep application from temporal 'freezing'
    update_ui()

do_something_fancy(lines)
```

## future operator

Sometimes it's not desirable to access value of _Future object_ (and thus
potentially halt thread until value is computed), but working with variable
which was _Future object_ assigned into is needed. In that case, future operator
can be used.

future operator returns actual value of variable, even if variable is
_Future object_. Unlike every other possible use, accessing _Future object_ with
future operator will _not_ replace it with computed value nor wait for this
value to be computed.

Some examples:

```
x = future : read_data()
y = x
```

y will contain value returned from read_data(). Thread will be suspended until
read_data() is finished.

```
x = future : read_data()
y = future x
```

Both x and y will contain same Future object. Thread may be suspended only
with next access to x _or_ y.

```
def get_data():
    f = get_filename()
    x = future : read_data(f)
    return future x
```
function will return _Future object_ that computes result of read_data in
parallel thread. Thread may be suspended only if return value is stored and
accessed afterwards.

# Risks and caveats

## Error handling

Error handling may deliver some unpleasant surprises when working with
futures. Exception thrown in another thread cannot be pushed to original thread
randomly. Instead, exception is saved and thrown only when computed value is
requested.

Let's reuse former example:

```
lines = future : file("data.txt").read().split("\n")
while not ready lines:
    # keep application from temporal 'freezing'
    update_ui()

do_something_fancy(lines)
```

Should 'data.txt' be not accessible, IOError will be raised. This will make
_Future object_ immediately [ready](#ready-operator), but accessing the 'lines'
variable will thrown accumulated IOError on last line, i.e. at place, where
it's not generally expected.

Best way how to handle such situations is to place entire block working with
future into try...except block:

```
try:
    lines = future : file("data.txt").read().split("\n")
    while not ready lines:
        # keep application from temporal 'freezing'
        update_ui()
    
    do_something_fancy(lines)
except IOError, e:
    display_error(e)
```

This property of futures should be always considered when function that uses
[future operator](#future-operator) to return _Future object_ is created as
user of this function may not be aware of the fact that returned value may throw
exception.

# Variable scope

Expression used by _Future object_ gets its variable scope _copied_ from block
in which it is created. That means that all variables available in block are
available in expression, but values are preserved even if value in parent scope
is changed.

For example in,

```
x = 10
y = future : 10 * x
x = 5
```

value of 'y' will be always 100, no matter if original thread manages to change
value of 'x' before computation is done in parallel thread.

On other hand, object references are copied, but copy still points at same
object. This may introduce unpredictable behaviour if original thread changes
state of object that parallel uses.

In another example:

```
data = file("data.txt")
lines = future : data.read().split("\n")
data.close()
```

value of lines cannot be predicted, as there is no way to tell if close() method
will be called before read() operation is finished. Code like this may produce
different results even between multiple runs.

