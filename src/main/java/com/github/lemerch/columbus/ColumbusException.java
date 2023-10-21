/**
 * Copyright 2023 Dmitry Terakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * \ \ .       .             \                  O’er the glad waters of the dark
 * . \ \       |\            / \_               blue sea,
 * \ . .       | \            \                 Our thoughts as boundless, and
 * . \         |  \            \                our souls as free,
 *  .          |   \           /                Far as the breeze can bear, the
 *             |    \          \                billows foam,
 *             |     \                          Survey our empire, and behold
 *             |      \                         our home.
 *             |       \       ____O                             «The Corsair». L. Byron
 *             |        \     .' ./
 *             |   _.,-~"\  .',/~'
 *             &lt;-~"   _.,-~" ~ |
 * ^"~-,._.,-~"^"~-,._\       /,._.,-~"^"~-,._.,-~"^"~-,._
 * ~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
 * ^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
 * ~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^ COLUMBUS - the discoverer of convenience mapping
 */
package com.github.lemerch.columbus;

public class ColumbusException extends RuntimeException {
    public ColumbusException(String message) { super(message); }
    public ColumbusException(Exception exception) { super(exception); }
}
